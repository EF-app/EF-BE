package com.nokcha.efbe.infra.scheduler.match;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.feed.ColdStartFeed;
import com.nokcha.efbe.domain.match.feed.MyFeedRecomputer;
import com.nokcha.efbe.domain.match.repository.KeywordFreqServiceImpl;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 매일 04:00 KST 매칭 배치 + 05:00 KST 보정 배치 (명세서 §10.16 / §10.19).
 *
 *  04:00 정상 흐름:
 *    1) MatchingConfig 로드 (1회)
 *    2) KeywordFreqService 캐시 갱신
 *    3) 활성 뷰어 순회:
 *         a. 후보 풀 500 (CandidateSelector)
 *         b. 페어별 점수 + 태그 (MatchCalculator) + match_score_cache upsert
 *         c. 슬롯 50 선정 (FeedSelector) → match_daily_feed 교체
 *
 *  05:00 보정 흐름 (retryFailedViewers):
 *    04:00 에 실패해 오늘 daily_feed row 가 없는 활성 viewer 만 재시도.
 *    "오늘 갱신된 viewer = match_daily_feed.feed_date = CURDATE() 의 viewer_id 집합" 으로 식별.
 *
 *  ShedLock 으로 멀티 인스턴스 동시 실행 방지.
 *  뷰어당 트랜잭션 분리 — 한 명 실패해도 다른 뷰어 진행.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NightlyMatchBatch {

    private final MatchingConfigLoader configLoader;
    private final KeywordFreqServiceImpl keywordFreqService;
    private final UserManagement userMgmt;
    private final MyFeedRecomputer recomputer;
    private final ColdStartFeed coldStartFeed;

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "NightlyMatchBatch.run",
            lockAtMostFor = "PT2H", lockAtLeastFor = "PT5M")
    public void run() {
        long start = System.currentTimeMillis();
        MatchingConfig cfg = configLoader.load();
        keywordFreqService.refresh();

        LocalDate today = LocalDate.now();
        int viewerCount = 0;
        int failCount = 0;

        List<UserContext> viewers = userMgmt.findEligibleViewers(cfg);
        log.info("[NightlyMatchBatch] 시작 — 뷰어 {} 명, 날짜 {}", viewers.size(), today);

        for (UserContext me : viewers) {
            try {
                recomputer.process(me, cfg, today);
                viewerCount++;
            } catch (Exception e) {
                failCount++;
                log.warn("[NightlyMatchBatch] 뷰어 처리 실패 — userId={}, err={}", me.id(), e.getMessage(), e);
            }
        }

        long ms = System.currentTimeMillis() - start;
        log.info("[NightlyMatchBatch] 완료 — 성공 {}, 실패 {}, 소요 {}ms", viewerCount, failCount, ms);
    }

    /**
     * 05:00 KST 보정 배치 — 04:00 정상 배치가 실패해 오늘 daily_feed row 가 없는 활성 viewer 만 재시도.
     *
     *  "실패자" 식별 = 활성+승인+최근접속 viewer 중 오늘 match_daily_feed 에 row 없음
     *    → 일회성 에러(deadlock, connection timeout 등) 의 자동 흡수
     *  보정 배치도 실패하면 → 다음날 04:00 에 또 시도. 영구 실패는 모니터링/디버깅 대상.
     *
     *  ※ ColdStartFeed 실패 (가입 직후 임시 피드 미생성) 도 같은 SQL 로 포착되어 함께 복구된다.
     */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "NightlyMatchBatch.retryFailedViewers",
            lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void retryFailedViewers() {
        long start = System.currentTimeMillis();
        MatchingConfig cfg = configLoader.load();

        LocalDate today = LocalDate.now();
        List<UserContext> failed = userMgmt.findFailedViewersToday(cfg);
        if (failed.isEmpty()) {
            log.info("[NightlyMatchBatch.retry] 보정 대상 없음 — 04:00 배치가 모두 성공");
            return;
        }
        log.info("[NightlyMatchBatch.retry] 시작 — 보정 대상 {} 명, 날짜 {}", failed.size(), today);

        int recoverCount = 0;
        int coldStartCount = 0;
        int failCount = 0;
        for (UserContext me : failed) {
            try {
                recomputer.process(me, cfg, today);   // 1차: 정상 흐름 (+ 풀 0명 시 내부 ColdStartFeed fallback)
                recoverCount++;
            } catch (Exception e) {
                log.warn("[NightlyMatchBatch.retry] 정상 흐름 실패 → ColdStartFeed 마지막 시도 — userId={}, err={}",
                        me.id(), e.getMessage(), e);
                try {
                    coldStartFeed.build(me, cfg);     // 2차: ColdStartFeed 직접 시도
                    coldStartCount++;
                } catch (Exception e2) {
                    failCount++;
                    log.warn("[NightlyMatchBatch.retry] ColdStartFeed 도 실패 — userId={}, err={}",
                            me.id(), e2.getMessage(), e2);
                }
            }
        }

        long ms = System.currentTimeMillis() - start;
        log.info("[NightlyMatchBatch.retry] 완료 — 정상복구 {}, ColdStart복구 {}, 재실패 {}, 소요 {}ms",
                recoverCount, coldStartCount, failCount, ms);
    }
}
