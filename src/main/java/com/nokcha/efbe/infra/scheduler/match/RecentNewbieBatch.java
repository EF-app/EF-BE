package com.nokcha.efbe.infra.scheduler.match;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.feed.FeedSelector;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
 *    3) viewer 별로 reserved rank 오름차순 순회 + INSERT IGNORE
 *       — PK 충돌 (이미 채워짐) 시 silent skip, 첫 빈 자리에 INSERT 1회 후 break
 *       — 모든 reserved 차있으면 INSERT 0
 *
 *  04:00 정상 배치가 viewer 전체 row 를 DELETE+INSERT 하므로 FRESH_NEWBIE row 자동 정리.
 *  read-time 오버레이가 newcomer 의 status/profile_status/block 변화 즉시 반영.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecentNewbieBatch {

    private final EntityManager em;
    private final MatchingConfigLoader configLoader;
    private final UserManagement userMgmt;

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
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

    @SuppressWarnings("unchecked")
    private List<Long> findRecentNewcomers(LocalDateTime since) {
        return ((List<Number>) em.createNativeQuery("""
                SELECT u.id FROM users u
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE u.create_time >= :since
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                """)
                .setParameter("since", since)
                .getResultList()).stream()
                .map(Number::longValue).toList();
    }

    /**
     * newcomer 1명 → 호환 viewer 의 daily_feed 예약 rank 중 비어있는 첫 자리에 INSERT.
     *  reserved rank 오름차순 순회 — INSERT IGNORE 가 PK 충돌 시 0 반환 → 다음 reserved 시도.
     *  성공 시 viewer 당 1회만 등장 (break).
     */
    private int fanOut(long newcomerId, MatchingConfig cfg, List<Integer> reservedRanks) {
        List<Long> viewerIds = userMgmt.findCompatibleViewerIds(
                newcomerId, cfg.getFreshNewbieFanOut(), cfg);

        int inserted = 0;
        for (Long viewerId : viewerIds) {
            for (Integer r : reservedRanks) {
                int rows = em.createNativeQuery("""
                        INSERT IGNORE INTO match_daily_feed
                               (feed_date, viewer_id, `rank`, target_id, sort_key, slot_type, tags_json)
                        VALUES (CURDATE(), :v, :r, :newcomer, 0, 'FRESH_NEWBIE', '[]')
                        """)
                        .setParameter("v", viewerId)
                        .setParameter("r", r)
                        .setParameter("newcomer", newcomerId)
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
