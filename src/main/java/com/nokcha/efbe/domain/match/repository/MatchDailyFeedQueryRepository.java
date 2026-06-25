package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.common.util.JdbcTimeMapper;
import com.nokcha.efbe.domain.match.entity.MatchDailyFeed;
import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.repository.projection.FeedView;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *  - {@link #findCurrentFeed}: 피드 조회 시 read-time 오버레이
 *  - {@link #countByViewerId}: viewer 의 row 수 — lazy fallback 판단용
 *  - {@link #replaceDailyFeed}: viewer 의 모든 row 를 지우고 새로 저장 (배치/콜드스타트/단건 재계산)
 *
 *  ── read-time 오버레이 ──
 *    - target status≠ACTIVE 또는 profile_status≠APPROVED → 제외 (정지/탈퇴/미승인)
 *    - block 양방향 → 제외 (한낮 차단 즉시 반영)
 *    - viewer 가 이미 액션 (LIKE/SUPER_LIKE/POWER_MESSAGE) 또는 PASS 미만료 → 제외
 *      (본 카드는 다음 호출에서 자동으로 응답에서 빠짐 → 미니 배치의 신규자가 자연스럽게 등장)
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MatchDailyFeedQueryRepository {

    private final EntityManager em;

    /**
     * 현재 피드 조회 (read-time 오버레이 + 카드 표시 필드 join + 거리 km 계산).
     *  - rank 오름차순
     *  - 대표 사진: user_profile_image 의 sort_order 가장 낮은 것 (서브쿼리)
     *  - 거리: {@code ST_Distance_Sphere}(POINT(viewer 좌표), POINT(target 좌표)) / 1000 (km)
     *           viewer 또는 target 의 area 가 null 이면 distanceKm = null
     */
    public List<FeedView> findCurrentFeed(long viewerId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                SELECT f.match_rank, f.target_id, f.slot_type, f.tags_json,
                       u.nickname, u.age,
                       up.mbti, up.job, up.bio_message,
                       ca.country, ca.city,
                       (SELECT pi.url FROM user_profile_image pi
                         WHERE pi.user_id = f.target_id
                         ORDER BY pi.sort_order ASC
                         LIMIT 1) AS main_photo_url,
                       CASE
                         WHEN me_ca.latitude IS NULL OR ca.latitude IS NULL THEN NULL
                         ELSE ST_Distance_Sphere(
                                  POINT(me_ca.longitude, me_ca.latitude),
                                  POINT(ca.longitude, ca.latitude)
                              ) / 1000.0
                       END AS distance_km,
                       u.last_active_at
                  FROM match_daily_feed f
                  JOIN users u         ON u.id = f.target_id
                  JOIN user_profile up ON up.user_id = f.target_id
                  LEFT JOIN code_area ca   ON ca.id = u.area_id
                  JOIN users me_u           ON me_u.id = :v
                  LEFT JOIN code_area me_ca ON me_ca.id = me_u.area_id
                 WHERE f.viewer_id = :v
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND NOT EXISTS (
                       SELECT 1 FROM block b
                        WHERE (b.blocker_id = :v AND b.blocked_id = f.target_id)
                           OR (b.blocker_id = f.target_id AND b.blocked_id = :v)
                   )
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma
                        WHERE ma.actor_id = :v
                          AND ma.target_id = f.target_id
                          AND (
                              ma.action_type IN ('LIKE','SUPER_LIKE','POWER_MESSAGE')
                              OR (ma.action_type = 'PASS' AND ma.expires_at >= NOW())
                          )
                   )
                 ORDER BY f.match_rank
                """)
                .setParameter("v", viewerId)
                .getResultList();

        List<FeedView> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new FeedView(
                    ((Number) r[0]).intValue(),
                    ((Number) r[1]).longValue(),
                    (String) r[2],
                    (String) r[3],
                    (String) r[4],
                    r[5] == null ? null : ((Number) r[5]).intValue(),
                    (String) r[6],
                    (String) r[7],
                    (String) r[8],
                    (String) r[9],
                    (String) r[10],
                    (String) r[11],
                    r[12] == null ? null : ((Number) r[12]).doubleValue(),
                    JdbcTimeMapper.toLocalDateTimeOrNull(r[13])
            ));
        }
        return result;
    }

    /**
     *  본인이 다 액션해서 오버레이로 0건이 된 경우 vs 04:00 배치 자체가 안 돈 경우 구분 (lazy fallback).
     */
    public int countByViewerId(long viewerId) {
        return ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM match_daily_feed WHERE viewer_id = :v")
                .setParameter("v", viewerId)
                .getSingleResult()).intValue();
    }

    /**
     * viewer 의 모든 row 를 교체 — DELETE (전체 날짜) + INSERT.
     *  feed_date 는 생성 시각의 날짜로 저장
     */
    @Transactional
    public void replaceDailyFeed(long viewerId, LocalDate date, List<DailyFeedRow> rows) {
        int deleted = em.createNativeQuery(
                "DELETE FROM match_daily_feed WHERE viewer_id = :v")
                .setParameter("v", viewerId)
                .executeUpdate();
        if (deleted > 0) {
            log.debug("[DailyFeed] 기존 row 제거 — viewer={}, count={}", viewerId, deleted);
        }
        if (rows == null || rows.isEmpty()) return;

        List<MatchDailyFeed> entities = new ArrayList<>(rows.size());
        for (DailyFeedRow r : rows) {
            entities.add(MatchDailyFeed.builder()
                    .feedDate(date)
                    .viewerId(viewerId)
                    .matchRank((short) r.matchRank())
                    .targetId(r.targetId())
                    .sortKey(BigDecimal.valueOf(r.sortKey()).setScale(4, RoundingMode.HALF_UP))
                    .slotType(MatchDailyFeed.SlotType.valueOf(r.slotType()))
                    .tagsJson(r.tagsJson())
                    .build());
        }
        for (MatchDailyFeed e : entities) em.persist(e);
        em.flush();
    }
}
