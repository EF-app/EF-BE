package com.nokcha.efbe.domain.match.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.ActivityStatusResolver;
import com.nokcha.efbe.domain.match.dto.response.MatchLikeUserDto;
import com.nokcha.efbe.domain.match.dto.response.MutualMatchItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.ReceivedLikeItemRspDto;
import com.nokcha.efbe.domain.match.dto.response.SentLikeItemRspDto;
import com.nokcha.efbe.domain.match.repository.MatchListQueryRepository;
import com.nokcha.efbe.domain.match.repository.projection.LikeActionRow;
import com.nokcha.efbe.domain.match.repository.projection.MutualMatchRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 매칭 목록 (received / sent / mutual) — 정책 적용 + DTO 조립 + cursor.
 *  데이터 액세스는 {@link MatchListQueryRepository} 위임. 이 클래스는:
 *   - 정책 상수 (7일 cutoff / 3h fresh / 10분 online / 3일 super pin)
 *   - JSON 파싱 (tags_json → chips 평탄화 + matchScore)
 *   - region 조립, isOnline / isSuper / isFresh 계산
 *   - cursor encode + CursorPageResponse wrapping
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

    private final MatchListQueryRepository repo;

    /* ───────── COUNT ───────── */

    public int countSent(long meId) {
        return repo.countSent(meId, listCutoff());
    }

    public int countReceived(long meId) {
        return repo.countReceived(meId, listCutoff());
    }

    public int countMutual(long meId) {
        return repo.countMutual(meId, listCutoff());
    }

    /* ───────── SEARCH ───────── */

    public CursorPageResponse<ReceivedLikeItemRspDto> searchReceived(long meId, Long cursorId, int size) {
        List<LikeActionRow> rows = repo.searchReceived(meId, cursorId, size + 1, listCutoff());
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime superPinThreshold = LocalDateTime.now().minusDays(SUPER_PIN_DAYS_RECEIVED);

        List<ReceivedLikeItemRspDto> items = new ArrayList<>(rows.size());
        Long lastId = null;
        for (LikeActionRow r : rows) {
            boolean isSuper = "SUPER_LIKE".equals(r.actionType()) && r.createdAt().isAfter(superPinThreshold);
            MatchLikeUserDto user = toUserDto(r, onlineThreshold);
            items.add(new ReceivedLikeItemRspDto(
                    String.valueOf(r.actionId()), r.createdAt().toString(), isSuper, user
            ));
            lastId = r.actionId();
        }

        String nextCursor = hasMore && lastId != null ? String.valueOf(lastId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    public CursorPageResponse<SentLikeItemRspDto> searchSent(long meId, Long cursorId, int size) {
        List<LikeActionRow> rows = repo.searchSent(meId, cursorId, size + 1, listCutoff());
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime superPinThreshold = LocalDateTime.now().minusDays(SUPER_PIN_DAYS_RECEIVED);

        List<SentLikeItemRspDto> items = new ArrayList<>(rows.size());
        Long lastId = null;
        for (LikeActionRow r : rows) {
            boolean isSuper = "SUPER_LIKE".equals(r.actionType()) && r.createdAt().isAfter(superPinThreshold);
            MatchLikeUserDto user = toUserDto(r, onlineThreshold);
            items.add(new SentLikeItemRspDto(
                    String.valueOf(r.actionId()), r.createdAt().toString(), isSuper, user
            ));
            lastId = r.actionId();
        }

        String nextCursor = hasMore && lastId != null ? String.valueOf(lastId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    public CursorPageResponse<MutualMatchItemRspDto> searchMutual(long meId, Long cursorId, int size) {
        List<MutualMatchRow> rows = repo.searchMutual(meId, cursorId, size + 1, listCutoff());
        boolean hasMore = rows.size() > size;
        if (hasMore) rows = rows.subList(0, size);

        LocalDateTime onlineThreshold = LocalDateTime.now().minusMinutes(ONLINE_THRESHOLD_MINUTES);
        LocalDateTime freshThreshold  = LocalDateTime.now().minusHours(MUTUAL_FRESH_HOURS);

        List<MutualMatchItemRspDto> items = new ArrayList<>(rows.size());
        Long lastMatchId = null;
        for (MutualMatchRow r : rows) {
            boolean isFresh = r.matchedAt().isAfter(freshThreshold);
            boolean isOnline = r.lastActiveAt() != null && r.lastActiveAt().isAfter(onlineThreshold);
            String region = composeRegion(r.country(), r.city());
            ParsedTags parsed = parseTags(r.tagsJson());

            MatchLikeUserDto user = new MatchLikeUserDto(
                    String.valueOf(r.userId()), r.nickname(), r.age(), region,
                    parsed.tags(), parsed.matchScore(), isOnline, r.mainPhotoUrl(),
                    r.bioMessage(), r.distanceKm(),
                    ActivityStatusResolver.resolve(r.lastActiveAt())
            );
            items.add(new MutualMatchItemRspDto(
                    String.valueOf(r.matchId()),
                    r.matchedAt().toString(),
                    isFresh,
                    r.isSuper(),
                    r.chatRoomId() == null ? null : String.valueOf(r.chatRoomId()),
                    user
            ));
            lastMatchId = r.matchId();
        }

        String nextCursor = hasMore && lastMatchId != null ? String.valueOf(lastMatchId) : null;
        return new CursorPageResponse<>(items, nextCursor, hasMore);
    }

    /* ───────── 내부 헬퍼 ───────── */

    private static LocalDateTime listCutoff() {
        return LocalDateTime.now().minusDays(LIST_CUTOFF_DAYS);
    }

    private MatchLikeUserDto toUserDto(LikeActionRow r, LocalDateTime onlineThreshold) {
        boolean isOnline = r.lastActiveAt() != null && r.lastActiveAt().isAfter(onlineThreshold);
        String region = composeRegion(r.country(), r.city());
        ParsedTags parsed = parseTags(r.tagsJson());
        return new MatchLikeUserDto(
                String.valueOf(r.userId()), r.nickname(), r.age(), region,
                parsed.tags(), parsed.matchScore(), isOnline, r.mainPhotoUrl(),
                r.bioMessage(), r.distanceKm(),
                ActivityStatusResolver.resolve(r.lastActiveAt())
        );
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
