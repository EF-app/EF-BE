package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.repository.projection.LikeActionRow;
import com.nokcha.efbe.domain.match.repository.projection.MutualMatchRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 매칭 목록 (received / sent / mutual) — 카운트 + cursor list native query 데이터 액세스.
 *  - 정책 상수 (7일 cutoff 등) 는 호출자 (Service) 에서 cutoff 값을 계산해 인자로 주입
 *  - read-time 오버레이: 상대방 ACTIVE+APPROVED + 양방향 차단 없음 (SQL 내장)
 */
@Repository
@RequiredArgsConstructor
public class MatchListRepository {

    private final EntityManager em;

    /* ───────── COUNT ───────── */

    public int countSent(long meId, LocalDateTime cutoff) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.target_id
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE ma.actor_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= :cutoff
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = ma.target_id
                          AND ma2.target_id = ma.actor_id
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= :cutoff
                   )
                """)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return n.intValue();
    }

    public int countReceived(long meId, LocalDateTime cutoff) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.actor_id
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE ma.target_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= :cutoff
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions my
                        WHERE my.actor_id = :me
                          AND my.target_id = ma.actor_id
                          AND (
                              my.action_type IN ('LIKE','SUPER_LIKE')
                              OR (my.action_type = 'PASS' AND my.expires_at >= NOW())
                          )
                   )
                """)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return n.intValue();
    }

    public int countMutual(long meId, LocalDateTime cutoff) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                  FROM match_actions ma1
                  JOIN match_actions ma2
                    ON ma2.actor_id = ma1.target_id
                   AND ma2.target_id = ma1.actor_id
                  JOIN users u         ON u.id = ma1.target_id
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE ma1.actor_id = :me
                   AND ma1.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma1.create_time >= :cutoff
                   AND ma2.create_time >= :cutoff
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                """)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff)
                .getSingleResult();
        return n.intValue();
    }

    /* ───────── SEARCH ───────── */

    public List<LikeActionRow> searchReceived(long meId, Long cursorId, int limit, LocalDateTime cutoff) {
        StringBuilder where = new StringBuilder("""
                 WHERE ma.target_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= :cutoff
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions my
                        WHERE my.actor_id = :me
                          AND my.target_id = ma.actor_id
                          AND (
                              my.action_type IN ('LIKE','SUPER_LIKE')
                              OR (my.action_type = 'PASS' AND my.expires_at >= NOW())
                          )
                   )
                """);
        if (cursorId != null) where.append(" AND ma.id < :cursorId ");

        String sql = """
                SELECT ma.id, ma.action_type, ma.create_time, ma.tags_json,
                       u.id, u.nickname, u.age, u.last_active_at,
                       ca.country, ca.city,
                       (SELECT upi.url FROM user_profile_image upi
                         WHERE upi.user_id = u.id
                         ORDER BY upi.sort_order LIMIT 1) AS main_photo_url,
                       up.bio_message,
                       CASE
                         WHEN me_ca.latitude IS NULL OR ca.latitude IS NULL THEN NULL
                         ELSE ST_Distance_Sphere(
                                  POINT(me_ca.longitude, me_ca.latitude),
                                  POINT(ca.longitude, ca.latitude)
                              ) / 1000.0
                       END AS distance_km
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.actor_id
                  JOIN user_profile up ON up.user_id = u.id
                  LEFT JOIN code_area ca    ON ca.id = u.area_id
                  JOIN users me_u           ON me_u.id = :me
                  LEFT JOIN code_area me_ca ON me_ca.id = me_u.area_id
                """ + where + " ORDER BY ma.id DESC LIMIT " + limit;

        Query q = em.createNativeQuery(sql)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return mapLikeActionRows(rows);
    }

    public List<LikeActionRow> searchSent(long meId, Long cursorId, int limit, LocalDateTime cutoff) {
        StringBuilder where = new StringBuilder("""
                 WHERE ma.actor_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= :cutoff
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = ma.target_id
                          AND ma2.target_id = ma.actor_id
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= :cutoff
                   )
                """);
        if (cursorId != null) where.append(" AND ma.id < :cursorId ");

        String sql = """
                SELECT ma.id, ma.action_type, ma.create_time, ma.tags_json,
                       u.id, u.nickname, u.age, u.last_active_at,
                       ca.country, ca.city,
                       (SELECT upi.url FROM user_profile_image upi
                         WHERE upi.user_id = u.id
                         ORDER BY upi.sort_order LIMIT 1) AS main_photo_url,
                       up.bio_message,
                       CASE
                         WHEN me_ca.latitude IS NULL OR ca.latitude IS NULL THEN NULL
                         ELSE ST_Distance_Sphere(
                                  POINT(me_ca.longitude, me_ca.latitude),
                                  POINT(ca.longitude, ca.latitude)
                              ) / 1000.0
                       END AS distance_km
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.target_id
                  JOIN user_profile up ON up.user_id = u.id
                  LEFT JOIN code_area ca    ON ca.id = u.area_id
                  JOIN users me_u           ON me_u.id = :me
                  LEFT JOIN code_area me_ca ON me_ca.id = me_u.area_id
                """ + where + " ORDER BY ma.id DESC LIMIT " + limit;

        Query q = em.createNativeQuery(sql)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return mapLikeActionRows(rows);
    }

    public List<MutualMatchRow> searchMutual(long meId, Long cursorId, int limit, LocalDateTime cutoff) {
        StringBuilder where = new StringBuilder("""
                 WHERE (mr.user_a_id = :me OR mr.user_b_id = :me)
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND EXISTS (
                       SELECT 1 FROM match_actions ma1
                        WHERE ma1.actor_id = :me AND ma1.target_id = u.id
                          AND ma1.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma1.create_time >= :cutoff
                   )
                   AND EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = u.id AND ma2.target_id = :me
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= :cutoff
                   )
                """);
        if (cursorId != null) where.append(" AND mr.id < :cursorId ");

        String sql = """
                SELECT mr.id AS match_id,
                       mr.create_time AS matched_at,
                       (SELECT ma1.tags_json FROM match_actions ma1
                         WHERE ma1.actor_id = :me AND ma1.target_id = u.id
                           AND ma1.action_type IN ('LIKE','SUPER_LIKE')
                         LIMIT 1) AS tags_json,
                       mr.is_super,
                       mr.chat_room_id,
                       u.id, u.nickname, u.age, u.last_active_at,
                       ca.country, ca.city,
                       (SELECT upi.url FROM user_profile_image upi
                         WHERE upi.user_id = u.id
                         ORDER BY upi.sort_order LIMIT 1) AS main_photo_url,
                       up.bio_message,
                       CASE
                         WHEN me_ca.latitude IS NULL OR ca.latitude IS NULL THEN NULL
                         ELSE ST_Distance_Sphere(
                                  POINT(me_ca.longitude, me_ca.latitude),
                                  POINT(ca.longitude, ca.latitude)
                              ) / 1000.0
                       END AS distance_km
                  FROM match_results mr
                  JOIN users u
                    ON u.id = CASE WHEN mr.user_a_id = :me THEN mr.user_b_id ELSE mr.user_a_id END
                  JOIN user_profile up ON up.user_id = u.id
                  LEFT JOIN code_area ca    ON ca.id = u.area_id
                  JOIN users me_u           ON me_u.id = :me
                  LEFT JOIN code_area me_ca ON me_ca.id = me_u.area_id
                """ + where + " ORDER BY mr.create_time DESC, mr.id DESC LIMIT " + limit;

        Query q = em.createNativeQuery(sql)
                .setParameter("me", meId)
                .setParameter("cutoff", cutoff);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<MutualMatchRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            long matchId = ((Number) r[0]).longValue();
            LocalDateTime matchedAt = toLocalDateTime(r[1]);
            String tagsJson = (String) r[2];
            // MariaDB BIT(1) → Boolean. 안전 분기 (Number 도 대응).
            boolean isSuper;
            if (r[3] instanceof Boolean b) isSuper = b;
            else if (r[3] instanceof Number nn) isSuper = nn.intValue() == 1;
            else isSuper = false;
            Long chatRoomId = r[4] == null ? null : ((Number) r[4]).longValue();
            long userId = ((Number) r[5]).longValue();
            String nickname = (String) r[6];
            Integer age = r[7] == null ? null : ((Number) r[7]).intValue();
            LocalDateTime lastActiveAt = r[8] == null ? null : toLocalDateTime(r[8]);
            String country = (String) r[9];
            String city = (String) r[10];
            String mainPhotoUrl = (String) r[11];
            String bioMessage = (String) r[12];
            Double distanceKm = r[13] == null ? null : ((Number) r[13]).doubleValue();
            result.add(new MutualMatchRow(matchId, matchedAt, tagsJson, isSuper, chatRoomId,
                    userId, nickname, age, lastActiveAt, country, city, mainPhotoUrl, bioMessage, distanceKm));
        }
        return result;
    }

    /* ───────── 헬퍼 ───────── */

    private static List<LikeActionRow> mapLikeActionRows(List<Object[]> rows) {
        List<LikeActionRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            long actionId = ((Number) r[0]).longValue();
            String actionType = (String) r[1];
            LocalDateTime createdAt = toLocalDateTime(r[2]);
            String tagsJson = (String) r[3];
            long userId = ((Number) r[4]).longValue();
            String nickname = (String) r[5];
            Integer age = r[6] == null ? null : ((Number) r[6]).intValue();
            LocalDateTime lastActiveAt = r[7] == null ? null : toLocalDateTime(r[7]);
            String country = (String) r[8];
            String city = (String) r[9];
            String mainPhotoUrl = (String) r[10];
            String bioMessage = (String) r[11];
            Double distanceKm = r[12] == null ? null : ((Number) r[12]).doubleValue();
            result.add(new LikeActionRow(actionId, actionType, createdAt, tagsJson,
                    userId, nickname, age, lastActiveAt, country, city, mainPhotoUrl, bioMessage, distanceKm));
        }
        return result;
    }

    private static LocalDateTime toLocalDateTime(Object v) {
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        throw new IllegalStateException("DATETIME 타입 예상 외: " + v.getClass());
    }
}
