package com.nokcha.efbe.infra.scheduler.match;

import com.nokcha.efbe.domain.match.calculator.MatchCalculator;
import com.nokcha.efbe.domain.match.calculator.SortKeyCalculator;
import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.feed.FeedSelector;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.match.tag.TagDisplayFormatter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 매 정시 신규자 fan-out — 04:00 정상 배치가 비워둔 예약 rank 자리에 신규자 채움.
 *
 *  슬롯 정책 ({@link FeedSelector#computeReservedRanks}):
 *    dailyShow=50, freshNewbieReservedSlots=5 → reserved = {10, 20, 30, 40, 50}
 *    04:00 배치는 reserved rank 를 skip 하고 나머지 자리 채움
 *    미니 배치는 viewer 의 reserved rank 중 비어있는 첫 자리에 INSERT (오름차순 시도)
 *
 *  흐름:
 *    1) 지난 {@code freshNewbieWindowHours} 시간 안에 가입한 ACTIVE+APPROVED 유저 추출
 *    2) 각 newcomer 별 호환 viewer 추출 — {@link UserManagement#findCompatibleViewerIds}
 *    3) viewer batch 컨텍스트 로드 → 페어 점수/태그/sortKey 계산
 *    4) viewer 별로 reserved rank 오름차순 순회 + INSERT IGNORE
 *       — PK 충돌 (이미 채워짐) 시 silent skip, 첫 빈 자리에 INSERT 1회 후 break
 *       — 모든 reserved 차있으면 INSERT 0
 *
 *  04:00 정상 배치가 viewer 전체 row 를 DELETE+INSERT 하므로 FRESH_NEWBIE row 자동 정리.
 *  read-time 오버레이가 newcomer 의 status/profile_status/block 변화 즉시 반영.
 *
 *  점수/태그 정책 (FRESH_NEWBIE 슬롯):
 *    sort_key/tags_json 정상 계산값 적용 — rank 만 reserved 자리에 강제 배치.
 *    카드 표시 시 (#키워드/#이상형/#가까운지역 등) 일반 슬롯과 일관된 태그 노출.
 *    created_at 은 NOW() 명시 (운영 sql_mode 가 NO_ZERO_DATE 미적용 시 안전장치).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecentNewbieBatch {

    private final EntityManager em;
    private final MatchingConfigLoader configLoader;
    private final UserManagement userMgmt;
    private final MatchCalculator matchCalculator;
    private final SortKeyCalculator sortKeyCalculator;
    private final TagDisplayFormatter tagFormatter;

    /**
     * 매시 30분 KST — 04:00 NightlyMatchBatch 와 시간차 30분 확보.
     *  매시 0분 트리거였을 때 04:00 정시에 두 배치가 동시 실행되어
     *  같은 viewer 의 match_daily_feed row 에 락 충돌 → fanOut 일부 실패 가능성이 있었음.
     *  30분 시프트로 NightlyMatchBatch (~10~20분 소요) 종료 후 진입 보장.
     */
    @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "RecentNewbieBatch.run",
            lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    @Transactional
    public void run() {
        long start = System.currentTimeMillis();
        MatchingConfig cfg = configLoader.load();

        List<Integer> reservedRanks = sortedReserved(cfg);
        if (reservedRanks.isEmpty()) {
            log.debug("[RecentNewbieBatch] 예약 자리 0 — 비활성");
            return;
        }

        LocalDateTime since = LocalDateTime.now().minusHours(cfg.getFreshNewbieWindowHours());
        List<Long> newcomers = findRecentNewcomers(since);
        if (newcomers.isEmpty()) {
            log.debug("[RecentNewbieBatch] 신규자 없음 — since={}", since);
            return;
        }

        int totalInserted = 0;
        for (Long newcomerId : newcomers) {
            try {
                totalInserted += fanOut(newcomerId, cfg, reservedRanks);
            } catch (Exception e) {
                log.warn("[RecentNewbieBatch] newcomer 처리 실패 — id={}, err={}",
                        newcomerId, e.getMessage(), e);
            }
        }

        long ms = System.currentTimeMillis() - start;
        log.info("[RecentNewbieBatch] 완료 — newcomers={}, inserts={}, 소요={}ms",
                newcomers.size(), totalInserted, ms);
    }

    private static List<Integer> sortedReserved(MatchingConfig cfg) {
        Set<Integer> reserved = FeedSelector.computeReservedRanks(cfg);
        return new ArrayList<>(new TreeSet<>(reserved));  // 오름차순
    }

    /**
     * 지난 freshNewbieWindowHours 시간 안에 가입한 신규자 ID 목록.
     *  최신 가입자 우선 처리 — viewer 의 reserved 5자리 (5/10/15/20/25) 가 INSERT IGNORE first-fit 으로
     *  채워지므로, 처리 순서가 자리 선점 순서가 됨. 신선도 보장을 위해 DESC.
     *
     *  인덱스 노트: users.create_time 별도 인덱스 없음 (PK + uk_* 만).
     *  24h 안 가입자만 필터 + status/profile_status 추가 필터 → 풀스캔이라도 결과 row 가 적음.
     *  users 행이 10만 넘으면 (create_time, status) 복합 인덱스 검토.
     */
    @SuppressWarnings("unchecked")
    private List<Long> findRecentNewcomers(LocalDateTime since) {
        return ((List<Number>) em.createNativeQuery("""
                SELECT u.id FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE u.create_time >= :since
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                 ORDER BY u.create_time DESC
                """)
                .setParameter("since", since)
                .getResultList()).stream()
                .map(Number::longValue).toList();
    }

    /**
     * newcomer 1명 → 호환 viewer 의 daily_feed 예약 rank 중 비어있는 첫 자리에 INSERT.
     *  reserved rank 오름차순 순회 — INSERT IGNORE 가 PK 충돌 시 0 반환 → 다음 reserved 시도.
     *  성공 시 viewer 당 1회만 등장 (break).
     *
     *  점수/태그 계산은 viewer 관점 (me=viewer, other=newcomer) — 카드 표시 일관성.
     */
    private int fanOut(long newcomerId, MatchingConfig cfg, List<Integer> reservedRanks) {
        UserContext newcomerCtx = userMgmt.loadContext(newcomerId);
        if (newcomerCtx == null) {
            log.warn("[RecentNewbieBatch] newcomer 컨텍스트 누락 — id={}", newcomerId);
            return 0;
        }

        /*
         * 호환 viewer 전체 → 메모리 셔플 → cap 만큼 자르기.
         *  SQL 의 `ORDER BY RAND() LIMIT cap` 는 후보 수 N 에 비례한 filesort 비용이 발생해 N 이 커질수록 비싸짐.
         *  메모리 셔플 (`Collections.shuffle` = Fisher–Yates O(n)) 로 대체:
         *    - 후보 ≤ 1만 수준에서 명확히 유리, 그 이상도 손해 없음.
         *    - 10만 이상으로 커지면 별도 sampling 패턴 (random offset / id 범위 / Bernoulli) 검토.
         */
        List<Long> allViewerIds = userMgmt.findCompatibleViewerIds(newcomerId, cfg);
        if (allViewerIds.isEmpty()) return 0;

        int cap = cfg.getFreshNewbieFanOut();
        List<Long> viewerIds;
        if (allViewerIds.size() <= cap) {
            viewerIds = new ArrayList<>(allViewerIds);
            Collections.shuffle(viewerIds);
        } else {
            List<Long> mutable = new ArrayList<>(allViewerIds);
            Collections.shuffle(mutable);
            viewerIds = mutable.subList(0, cap);
        }

        Map<Long, UserContext> viewerCtxByid = new HashMap<>(viewerIds.size() * 2);
        for (UserContext v : userMgmt.loadContexts(viewerIds)) {
            viewerCtxByid.put(v.id(), v);
        }

        int inserted = 0;
        for (Long viewerId : viewerIds) {
            UserContext viewerCtx = viewerCtxByid.get(viewerId);
            if (viewerCtx == null) continue;  // code_area 누락 등으로 컨텍스트 빌드 실패

            PairScore ps = matchCalculator.score(viewerCtx, newcomerCtx, cfg);
            double sortKey = sortKeyCalculator.calc(
                    viewerCtx, ps.keyword(), ps.idealBidir(),
                    ps.lifestyle(), ps.location(), cfg);
            String tagsJson = tagFormatter.renderJson(viewerCtx, ps);

            for (Integer r : reservedRanks) {
                int rows = em.createNativeQuery("""
                        INSERT IGNORE INTO match_daily_feed
                               (feed_date, viewer_id, `rank`, target_id, sort_key, slot_type, tags_json, created_at)
                        VALUES (CURDATE(), :v, :r, :newcomer, :sortKey, 'FRESH_NEWBIE', :tagsJson, NOW())
                        """)
                        .setParameter("v", viewerId)
                        .setParameter("r", r)
                        .setParameter("newcomer", newcomerId)
                        .setParameter("sortKey", sortKey)
                        .setParameter("tagsJson", tagsJson)
                        .executeUpdate();
                if (rows > 0) {
                    inserted++;
                    break;
                }
            }
        }
        return inserted;
    }
}
