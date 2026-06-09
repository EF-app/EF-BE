package com.nokcha.efbe.infra.scheduler.match;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.feed.ColdStartFeed;
import com.nokcha.efbe.domain.match.feed.MyFeedRecomputer;
import com.nokcha.efbe.domain.match.repository.KeywordFreqServiceImpl;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 매일 04:00 KST 매칭 배치 + 05:00 KST 보정 배치 (명세서 §10.16 / §10.19).
 *
 *  04:00 정상 흐름:
 *    1) MatchingConfig 로드 (1회)
 *    2) KeywordFreqService 캐시 갱신
 *    3) 활성 viewer 캐시 1회 로드
 *    4) 활성 뷰어 병렬 처리 (matchBatchExecutor):
 *         a. 후보 풀 500 (CandidateSelector)
 *         b. 페어별 점수 + 태그 (MatchCalculator) — 메모리만 계산 (§10.20 score_cache 야간 적재 제거)
 *         c. 슬롯 50 선정 (FeedSelector) → match_daily_feed 교체 (+ §10.21 emptyRanks 백필)
 *         d. 어제 set 과 같으면 DB write skip
 *    5) BatchPhaseMetrics 합산 로그
 *
 *  05:00 보정 흐름 (recoverFailedViewers):
 *    04:00 에 실패해 오늘 daily_feed row 가 없는 활성 viewer 만 재시도. 같은 병렬 흐름.
 *
 *  ShedLock 으로 멀티 인스턴스 동시 실행 방지.
 *  뷰어당 트랜잭션 분리 — 한 명 실패해도 다른 뷰어 진행 + 다른 thread 영향 X.
 */
@Slf4j
@Component
public class NightlyMatchBatch {

    private final MatchingConfigLoader configLoader;
    private final KeywordFreqServiceImpl keywordFreqService;
    private final UserManagement userMgmt;
    private final MyFeedRecomputer recomputer;
    private final ColdStartFeed coldStartFeed;
    private final ThreadPoolTaskExecutor matchBatchExecutor;

    /** mid-progress 로그 주기 (viewer N 명 처리 시마다 진행 보고). */
    private static final int PROGRESS_LOG_EVERY = 1000;

    public NightlyMatchBatch(MatchingConfigLoader configLoader,
                             KeywordFreqServiceImpl keywordFreqService,
                             UserManagement userMgmt,
                             MyFeedRecomputer recomputer,
                             ColdStartFeed coldStartFeed,
                             @Qualifier("matchBatchExecutor") ThreadPoolTaskExecutor matchBatchExecutor) {
        this.configLoader = configLoader;
        this.keywordFreqService = keywordFreqService;
        this.userMgmt = userMgmt;
        this.recomputer = recomputer;
        this.coldStartFeed = coldStartFeed;
        this.matchBatchExecutor = matchBatchExecutor;
    }

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "NightlyMatchBatch.run",
            lockAtMostFor = "PT2H", lockAtLeastFor = "PT5M")
    public void run() {
        runFullNow();
    }

    /**
     * 정상 배치 실제 본문 — ShedLock 없음. cron / 관리자 강제 트리거 모두에서 호출 가능.
     *
     *  - cron 진입은 {@link #run()} 의 @SchedulerLock 으로 멀티 인스턴스 중복 차단
     *  - 관리자 수동 트리거는 락 무관 — 이미 사람이 명시적으로 누른 행위
     *  - 활성 viewer 전체 대상 — 매번 DELETE + INSERT 패턴이라 idempotent
     *  - viewer 병렬 처리 — 각 viewer 가 독립 thread + 독립 transaction (process 의 inner-call 패턴)
     */
    public FullStats runFullNow() {
        long start = System.currentTimeMillis();

        long t0 = System.nanoTime();
        MatchingConfig cfg = configLoader.load();
        long cfgMs = (System.nanoTime() - t0) / 1_000_000;

        long t1 = System.nanoTime();
        keywordFreqService.refresh();
        long kwMs = (System.nanoTime() - t1) / 1_000_000;

        LocalDate today = LocalDate.now();

        // [§10.25-A] 활성 viewer 전체 캐시 1회 — viewer 1명당 loadContexts(N) 의 N² 부하 회피.
        //  배치 끝나면 자동 GC. 메모리: N × ~1KB ≈ N MB.
        long t2 = System.nanoTime();
        Map<Long, UserContext> activeCache = userMgmt.loadAllActiveContextsAsMap(cfg);
        long cacheMs = (System.nanoTime() - t2) / 1_000_000;

        log.info("[NightlyMatchBatch] 시작 — viewer {} 명, 날짜 {}, cfg {}ms / kwFreq {}ms / cache {}ms, threads={}",
                activeCache.size(), today, cfgMs, kwMs, cacheMs, matchBatchExecutor.getCorePoolSize());

        BatchPhaseMetrics metrics = new BatchPhaseMetrics();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        long viewerStart = System.currentTimeMillis();
        runViewersParallel(activeCache.values(), cfg, today, activeCache, metrics, successCount, failCount);
        long viewerMs = System.currentTimeMillis() - viewerStart;

        long ms = System.currentTimeMillis() - start;
        log.info("[NightlyMatchBatch] 완료 — 성공 {}, 실패 {}, 소요 {}ms (viewer loop {}ms, 평균 {}ms/viewer)",
                successCount.get(), failCount.get(), ms, viewerMs,
                successCount.get() == 0 ? 0 : viewerMs / successCount.get());
        log.info("[NightlyMatchBatch] phase 누계 (thread 합산) — {}", metrics.summary());
        return new FullStats(activeCache.size(), successCount.get(), failCount.get(), ms);
    }

    /** 정상 배치 실행 결과 — 관리자 강제 트리거 응답에 사용. */
    public record FullStats(int totalViewers, int successCount, int failCount, long durationMs) {}

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
    @SchedulerLock(name = "NightlyMatchBatch.recoverFailedViewers",
            lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void recoverFailedViewers() {
        runRecoverNow();
    }

    /**
     * 보정 배치 실제 본문 — ShedLock 없음. cron / BootCatchUp / 관리자 강제 트리거 모두에서 호출 가능.
     *
     *  - 정상 흐름 실패 시 ColdStartFeed 직접 시도 (2-Phase)
     *  - viewer 병렬 처리 동일
     */
    public RecoverStats runRecoverNow() {
        long start = System.currentTimeMillis();
        MatchingConfig cfg = configLoader.load();

        LocalDate today = LocalDate.now();
        List<UserContext> failed = userMgmt.findFailedViewersToday(cfg);
        if (failed.isEmpty()) {
            log.info("[NightlyMatchBatch.recover] 보정 대상 없음 — 04:00 배치가 모두 성공");
            return new RecoverStats(0, 0, 0, 0, System.currentTimeMillis() - start);
        }
        // [§10.25-A] 보정 대상이 클 경우에도 활성 viewer 전체 캐시 1회로 N² 회피.
        Map<Long, UserContext> activeCache = userMgmt.loadAllActiveContextsAsMap(cfg);
        log.info("[NightlyMatchBatch.recover] 시작 — 보정 대상 {} 명, 활성 viewer 캐시 {} 명, 날짜 {}, threads={}",
                failed.size(), activeCache.size(), today, matchBatchExecutor.getCorePoolSize());

        BatchPhaseMetrics metrics = new BatchPhaseMetrics();
        AtomicInteger recoverCount = new AtomicInteger();
        AtomicInteger coldStartCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<CompletableFuture<Void>> futures = new ArrayList<>(failed.size());
        for (UserContext me : failed) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    recomputer.process(me, cfg, today, activeCache, metrics);     // 1차: 정상 흐름 + 캐시
                    recoverCount.incrementAndGet();
                } catch (Exception e) {
                    log.warn("[NightlyMatchBatch.recover] 정상 흐름 실패 → ColdStartFeed 마지막 시도 — userId={}, err={}",
                            me.id(), e.getMessage(), e);
                    try {
                        coldStartFeed.build(me, cfg);                              // 2차: ColdStartFeed 직접 시도
                        coldStartCount.incrementAndGet();
                    } catch (Exception e2) {
                        failCount.incrementAndGet();
                        log.warn("[NightlyMatchBatch.recover] ColdStartFeed 도 실패 — userId={}, err={}",
                                me.id(), e2.getMessage(), e2);
                    }
                }
            }, matchBatchExecutor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long ms = System.currentTimeMillis() - start;
        log.info("[NightlyMatchBatch.recover] 완료 — 정상복구 {}, ColdStart복구 {}, 재실패 {}, 소요 {}ms",
                recoverCount.get(), coldStartCount.get(), failCount.get(), ms);
        log.info("[NightlyMatchBatch.recover] phase 누계 (thread 합산) — {}", metrics.summary());
        return new RecoverStats(failed.size(), recoverCount.get(), coldStartCount.get(), failCount.get(), ms);
    }

    /** 보정 배치 실행 결과 — 관리자 강제 트리거 응답에 사용. */
    public record RecoverStats(int targetCount, int recoverCount, int coldStartCount, int failCount, long durationMs) {}

    /**
     * §10.25 Step 3 — viewer 병렬 처리. 각 viewer 가 독립 thread + 독립 transaction.
     *  mid-progress 로그: PROGRESS_LOG_EVERY 마다 진행 보고.
     */
    private void runViewersParallel(Iterable<UserContext> viewers,
                                    MatchingConfig cfg, LocalDate today,
                                    Map<Long, UserContext> activeCache,
                                    BatchPhaseMetrics metrics,
                                    AtomicInteger successCount, AtomicInteger failCount) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (UserContext me : viewers) {
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    recomputer.process(me, cfg, today, activeCache, metrics);
                    int done = (int) metrics.viewersDone.sum();
                    metrics.viewersDone.increment();
                    successCount.incrementAndGet();
                    if ((done + 1) % PROGRESS_LOG_EVERY == 0) {
                        log.info("[NightlyMatchBatch] 진행 — {}/{} done, {}",
                                done + 1, "?", metrics.summary());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.warn("[NightlyMatchBatch] 뷰어 처리 실패 — userId={}, err={}",
                            me.id(), e.getMessage(), e);
                }
            }, matchBatchExecutor));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
