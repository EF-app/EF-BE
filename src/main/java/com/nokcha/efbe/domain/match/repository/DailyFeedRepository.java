package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.model.DailyFeedRow;
import com.nokcha.efbe.domain.match.entity.MatchDailyFeed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 통합 지점 3 — match_daily_feed 저장 + read-time 오버레이 조회.
 *  - {@link #replaceDailyFeed}: viewer 의 모든 row 를 지우고 새로 저장 (배치/콜드스타트/단건 재계산)
 *  - {@link #findCurrentFeed}: 피드 조회 시 read-time 오버레이
 *
 *  ── feed_date 의 의미 ──
 *    "노출 날짜" → "생성 날짜" 로 변경. viewer 당 row 1 세트만 유지.
 *    04:00 배치가 매일 viewer 전체 row 를 교체 — 자정 ~ 04:00 사이엔 어제 생성 피드 그대로 노출.
 *    명세서 §4.4 의 자정 리셋은 폐기 — UX 상 04:00 까지 빈 피드가 어색하다는 결정.
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
public class DailyFeedRepository {

    private final EntityManager em;

    /**
     * 현재 피드 조회 (read-time 오버레이 + 카드 표시 필드 join + 거리 km 계산).
     *  - viewer 당 1 세트만 있으므로 feed_date 필터 없이 viewer 의 모든 row
     *  - rank 오름차순
     *  - 대표 사진: user_profile_image 의 sort_order 가장 낮은 것 (서브쿼리)
     *  - 거리: {@code ST_Distance_Sphere}(POINT(viewer 좌표), POINT(target 좌표)) / 1000 (km)
     *           viewer 또는 target 의 area 가 null 이면 distanceKm = null
     *
     *  N+1 회피 — 1 쿼리에 user / profile / area / 대표 사진 / 거리 모두 처리.
     */
    public List<FeedView> findCurrentFeed(long viewerId) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
                SELECT f.`rank`, f.target_id, f.slot_type, f.tags_json,
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
                       END AS distance_km
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
                 ORDER BY f.`rank`
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
                    r[12] == null ? null : ((Number) r[12]).doubleValue()
            ));
        }
        return result;
    }

    /**
     * read-time 오버레이 적용 전 raw row 수 — viewer 의 daily_feed 자체가 비어있는지 판별.
     *  본인이 다 액션해서 오버레이로 0건이 된 경우 vs 04:00 배치 자체가 안 돈 경우 구분 (§10.20 lazy fallback).
     */
    public int countByViewerId(long viewerId) {
        return ((Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM match_daily_feed WHERE viewer_id = :v")
                .setParameter("v", viewerId)
                .getSingleResult()).intValue();
    }

    /** 피드 한 행의 표시용 view (read-time 결과 + 카드 표시 데이터 + 거리 km). */
    public record FeedView(
            int rank,
            long targetId,
            String slotType,
            String tagsJson,
            String nickname,
            Integer age,
            String mbti,
            String job,
            String bioMessage,
            String country,
            String city,
            String mainPhotoUrl,
            Double distanceKm
    ) {}

    /**
     * viewer 의 모든 row 를 교체 — DELETE (전체 날짜) + INSERT.
     *  feed_date 는 생성 시각의 날짜로 저장 (운영 디버깅용).
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
                    .rank((short) r.rank())
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
