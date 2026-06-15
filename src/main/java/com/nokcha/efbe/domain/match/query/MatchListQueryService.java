package com.nokcha.efbe.domain.match.query;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.domain.match.dto.response.MatchLikeUserDto;
import com.nokcha.efbe.domain.match.dto.response.MutualMatchItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.ReceivedLikeItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.SentLikeItemRspDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 매칭 목록 (received / sent / mutual) — 카운트 + cursor list native query 모음.
 *  - 정책 공통:
 *      · action_type IN ('LIKE','SUPER_LIKE')
 *      · create_time >= NOW() - INTERVAL 7 DAY  — 7일 유지 정책
 *      · 상대방 ACTIVE + APPROVED               — read-time 오버레이
 *      · 양방향 차단 없음                       — read-time 오버레이
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchListQueryService {

    private static final ObjectMapper OM = new ObjectMapper();
    private static final int ONLINE_THRESHOLD_MINUTES = 10;
    private static final int SUPER_PIN_DAYS_RECEIVED = 3;
    private static final int LIST_CUTOFF_DAYS = 7;
    private static final int MUTUAL_FRESH_HOURS = 3;   // 매칭 후 3h 안

    private final EntityManager em;

    /**
     * 내가 누른 좋아요 수 (LIKE+SUPER_LIKE, 7일 안, target 측 read-time 오버레이 통과).
     *  mutual 제외 — 양쪽 LIKE row 가 다 존재하는 페어는 we-like 화면 소관 (sent 에서 빠짐).
     */
    public int countSent(long meId) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.target_id
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE ma.actor_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= NOW() - INTERVAL 7 DAY
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = ma.target_id
                          AND ma2.target_id = ma.actor_id
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= NOW() - INTERVAL 7 DAY
                   )
                """)
                .setParameter("me", meId)
                .getSingleResult();
        return n.intValue();
    }

    /**
     * 받은 좋아요 수 (LIKE+SUPER_LIKE, 7일 안, 미응답만 카운트)
     *  - mutual 제외: NOT EXISTS 상대 LIKE row (양방향 LIKE)
     *  - 내 PASS 제외: NOT EXISTS 내 PASS row (받은좋아요 ✕ 처리, expires_at 만료 전)
     *  - 내 LIKE 제외: NOT EXISTS 내 LIKE row (받은좋아요 ❤ → mutual 로 이동)
     */
    public int countReceived(long meId) {
        Number n = (Number) em.createNativeQuery("""
                SELECT COUNT(*)
                  FROM match_actions ma
                  JOIN users u         ON u.id = ma.actor_id
                  JOIN user_profile up ON up.user_id = u.id
                 WHERE ma.target_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= NOW() - INTERVAL 7 DAY
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions my
                        WHERE my.actor_id = :me
                          AND my.target_id = ma.actor_id
                          AND (
                              my.action_type IN ('LIKE','SUPER_LIKE')                 -- mutual → 서로좋아요 화면
                              OR (my.action_type = 'PASS' AND my.expires_at >= NOW()) -- 내 PASS 처리
                          )
                   )
                """)
                .setParameter("me", meId)
                .getSingleResult();
        return n.intValue();
    }

    /**
     * 서로 좋아요 수 (양방향 LIKE+SUPER_LIKE 페어).
     *  - ma1 = 내가 상대에게 누른 row, ma2 = 상대가 나에게 누른 row
     *  - 양쪽 모두 7일 안 — 한쪽이라도 7일 지나면 한쪽 목록에서 빠지므로 mutual 도 빠짐 (일관성)
     *  - 상대방 ACTIVE+APPROVED + 양방향 차단 없음
     */
    public int countMutual(long meId) {
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
                   AND ma1.create_time >= NOW() - INTERVAL 7 DAY
                   AND ma2.create_time >= NOW() - INTERVAL 7 DAY
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                """)
                .setParameter("me", meId)
                .getSingleResult();
        return n.intValue();
    }


    /**
     * 받은 좋아요 목록 (INSERT 패턴).
     *  미응답 only — 내가 ❤ 누른 페어 (mutual 성사 → 서로좋아요 화면 소관) 와 내가 ✕ 누른 페어 사라짐
     *  정렬: ma.id DESC. isSuper: SUPER_LIKE AND create_time >= NOW - 3일.
     */
    public CursorPageResponse<ReceivedLikeItemRspDto> searchReceived(long meId, Long cursorId, int size) {
        StringBuilder where = new StringBuilder("""
                 WHERE ma.target_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= NOW() - INTERVAL %d DAY
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
                """.formatted(LIST_CUTOFF_DAYS));
        if (cursorId != null) where.append(" AND ma.id < :cursorId ");

        // me 좌표 + actor 좌표로 거리 계산. 둘 중 하나라도 null 이면 distance_km = null.
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
                """ + where + " ORDER BY ma.id DESC LIMIT " + (size + 1);

        Query q = em.createNativeQuery(sql).setParameter("me", meId);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime superPinThreshold = LocalDateTime.now().minusDays(SUPER_PIN_DAYS_RECEIVED);

        List<ReceivedLikeItemRspDto> items = new ArrayList<>(rows.size());
        Long lastId = null;
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

            boolean isSuper = "SUPER_LIKE".equals(actionType) && createdAt.isAfter(superPinThreshold);
            boolean isOnline = lastActiveAt != null && lastActiveAt.isAfter(onlineThreshold);
            String region = composeRegion(country, city);
            ParsedTags parsed = parseTags(tagsJson);

            MatchLikeUserDto user = new MatchLikeUserDto(
                    String.valueOf(userId), nickname, age, region,
                    parsed.tags(), parsed.matchScore(), isOnline, mainPhotoUrl,
                    bioMessage, distanceKm
            );
            items.add(new ReceivedLikeItemRspDto(
                    String.valueOf(actionId), createdAt.toString(), isSuper, user
            ));
            lastId = actionId;
        }

        String nextCursor = hasMore && lastId != null ? String.valueOf(lastId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    /**
     * 내가 누른 좋아요 목록 (cursor 기반).
     *  - mutual 제외 — 양쪽 LIKE row 가 다 있는 페어는 we-like 화면 소관
     *  - 정렬: ma.id DESC (= 시간 순), cursor 는 마지막 id
     *  - isSuper: SUPER_LIKE AND create_time >= NOW - 3일
     */
    public CursorPageResponse<SentLikeItemRspDto> searchSent(long meId, Long cursorId, int size) {
        StringBuilder where = new StringBuilder("""
                 WHERE ma.actor_id = :me
                   AND ma.action_type IN ('LIKE','SUPER_LIKE')
                   AND ma.create_time >= NOW() - INTERVAL %d DAY
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   AND NOT EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = ma.target_id
                          AND ma2.target_id = ma.actor_id
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= NOW() - INTERVAL %d DAY
                   )
                """.formatted(LIST_CUTOFF_DAYS, LIST_CUTOFF_DAYS));
        if (cursorId != null) where.append(" AND ma.id < :cursorId ");

        // me 좌표 + target 좌표로 거리 계산. 둘 중 하나라도 null 이면 distance_km = null.
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
                """ + where + " ORDER BY ma.id DESC LIMIT " + (size + 1);

        Query q = em.createNativeQuery(sql).setParameter("me", meId);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime superPinThreshold = LocalDateTime.now().minusDays(SUPER_PIN_DAYS_RECEIVED);

        List<SentLikeItemRspDto> items = new ArrayList<>(rows.size());
        Long lastId = null;
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

            boolean isSuper = "SUPER_LIKE".equals(actionType) && createdAt.isAfter(superPinThreshold);
            boolean isOnline = lastActiveAt != null && lastActiveAt.isAfter(onlineThreshold);
            String region = composeRegion(country, city);
            ParsedTags parsed = parseTags(tagsJson);

            MatchLikeUserDto user = new MatchLikeUserDto(
                    String.valueOf(userId), nickname, age, region,
                    parsed.tags(), parsed.matchScore(), isOnline, mainPhotoUrl,
                    bioMessage, distanceKm
            );
            items.add(new SentLikeItemRspDto(
                    String.valueOf(actionId), createdAt.toString(), isSuper, user
            ));
            lastId = actionId;
        }

        String nextCursor = hasMore && lastId != null ? String.valueOf(lastId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    /**
     * 서로 좋아요 목록 (cursor 기반 무한 스크롤).
     *  - match_results 기반 — mutual 페어 1 row 보존
     *  - is_super 컬럼 직접 사용 (self-join 불필요)
     *  - mutual cancel (양쪽 중 한쪽 PASS) 인 경우는 match_actions 조건으로 제외
     *  - 정렬: match_results.create_time DESC (mr.id DESC tie-break) — 최근 매칭 성사 순
     */
    public CursorPageResponse<MutualMatchItemRspDto> searchMutual(long meId, Long cursorId, int size) {
        StringBuilder where = new StringBuilder("""
                 WHERE (mr.user_a_id = :me OR mr.user_b_id = :me)
                   AND u.status = 'ACTIVE'
                   AND up.profile_status = 'APPROVED'
                   AND u.id NOT IN (SELECT b.blocked_id FROM block b WHERE b.blocker_id = :me)
                   AND u.id NOT IN (SELECT b.blocker_id FROM block b WHERE b.blocked_id = :me)
                   -- mutual cancel 된 페어 제외 — 양쪽 LIKE/SUPER_LIKE row 가 둘 다 있어야 mutual 유효
                   -- 7일 cutoff — countMutual 과 동기. mutual 도 sent/received 와 동일하게 7일 지나면 화면에서 사라짐 (match_results row 자체는 DB 영구 보존).
                   AND EXISTS (
                       SELECT 1 FROM match_actions ma1
                        WHERE ma1.actor_id = :me AND ma1.target_id = u.id
                          AND ma1.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma1.create_time >= NOW() - INTERVAL 7 DAY
                   )
                   AND EXISTS (
                       SELECT 1 FROM match_actions ma2
                        WHERE ma2.actor_id = u.id AND ma2.target_id = :me
                          AND ma2.action_type IN ('LIKE','SUPER_LIKE')
                          AND ma2.create_time >= NOW() - INTERVAL 7 DAY
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
                """ + where + " ORDER BY mr.create_time DESC, mr.id DESC LIMIT " + (size + 1);

        Query q = em.createNativeQuery(sql).setParameter("me", meId);
        if (cursorId != null) q.setParameter("cursorId", cursorId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime freshThreshold  = LocalDateTime.now().minusHours(MUTUAL_FRESH_HOURS);

        List<MutualMatchItemRspDto> items = new ArrayList<>(rows.size());
        Long lastMatchId = null;
        for (Object[] r : rows) {
            long matchId = ((Number) r[0]).longValue();
            LocalDateTime matchedAt = toLocalDateTime(r[1]);
            String tagsJson = (String) r[2];
            // MariaDB BIT(1) → Boolean. 안전 분기 (Number 도 대응).
            boolean isSuper;
            if (r[3] instanceof Boolean b) isSuper = b;
            else if (r[3] instanceof Number n) isSuper = n.intValue() == 1;
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

            boolean isFresh = matchedAt.isAfter(freshThreshold);
            boolean isOnline = lastActiveAt != null && lastActiveAt.isAfter(onlineThreshold);
            String region = composeRegion(country, city);
            ParsedTags parsed = parseTags(tagsJson);

            MatchLikeUserDto user = new MatchLikeUserDto(
                    String.valueOf(userId), nickname, age, region,
                    parsed.tags(), parsed.matchScore(), isOnline, mainPhotoUrl,
                    bioMessage, distanceKm
            );
            items.add(new MutualMatchItemRspDto(
                    String.valueOf(matchId),
                    matchedAt.toString(),
                    isFresh,
                    isSuper,
                    chatRoomId == null ? null : String.valueOf(chatRoomId),
                    user
            ));
            lastMatchId = matchId;
        }

        String nextCursor = hasMore && lastMatchId != null ? String.valueOf(lastMatchId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    /* ─── 내부 헬퍼 ─── */

    private static LocalDateTime toLocalDateTime(Object v) {
        if (v instanceof LocalDateTime ldt) return ldt;
        if (v instanceof Timestamp ts) return ts.toLocalDateTime();
        throw new IllegalStateException("DATETIME 타입 예상 외: " + v.getClass());
    }

    private static String composeRegion(String country, String city) {
        if (country == null && city == null) return "";
        if (country == null) return city;
        if (city == null) return country;
        return country + " " + city;
    }

    /**
     * tags_json 평탄화 — chips 최대 6개 + KEYWORD 의 percent 를 matchScore.
     *  형식: [{type, percent?, chips?, label?, categories?[{label, chips?}], star?}]
     */
    private ParsedTags parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return new ParsedTags(List.of(), 0);
        try {
            List<Map<String, Object>> arr = OM.readValue(tagsJson, new TypeReference<>() {});
            LinkedHashMap<String, Boolean> uniqueChips = new LinkedHashMap<>();
            int score = 0;
            for (Map<String, Object> tag : arr) {
                String type = (String) tag.get("type");
                if ("KEYWORD".equals(type) && tag.get("percent") instanceof Number n) {
                    score = Math.max(score, n.intValue());
                }
                Object chips = tag.get("chips");
                if (chips instanceof List<?> cl) {
                    for (Object c : cl) if (c instanceof String s) uniqueChips.put(s, true);
                }
                Object cats = tag.get("categories");
                if (cats instanceof List<?> catList) {
                    for (Object cat : catList) {
                        if (cat instanceof Map<?, ?> catMap) {
                            Object catChips = catMap.get("chips");
                            if (catChips instanceof List<?> ccl) {
                                for (Object c : ccl) if (c instanceof String s) uniqueChips.put(s, true);
                            }
                        }
                    }
                }
            }
            List<String> tags = new ArrayList<>(uniqueChips.keySet());
            if (tags.size() > 6) tags = tags.subList(0, 6);
            return new ParsedTags(tags, score);
        } catch (Exception e) {
            log.warn("[MatchLikes] tags_json parse 실패 — {}", e.getMessage());
            return new ParsedTags(List.of(), 0);
        }
    }

    private record ParsedTags(List<String> tags, int matchScore) {}
}
