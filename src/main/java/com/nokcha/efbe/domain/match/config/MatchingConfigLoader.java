package com.nokcha.efbe.domain.match.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.domain.match.repository.MatchConfigSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * code_match_config → {@link MatchingConfig} 매핑.
 *  - 키가 DB 에 없으면 {@code MatchingConfig} 의 코드 기본값 그대로 유지 (폴백)
 *  - JSON 필드(radius_steps_km / region_tiers / category_mate_cats) 는 Jackson 으로 파싱.
 *    파싱 실패 시 폴백 + warn 로그.
 *
 *  ※ 배치 시작 시 1 회만 호출 권장. 호출처: {@code NightlyMatchBatch.run()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingConfigLoader {

    private static final ObjectMapper OM = new ObjectMapper();

    private final MatchConfigSource source;

    public MatchingConfig load() {
        Map<String, String> m = source.findAllAsMap();
        MatchingConfig c = new MatchingConfig();

        /* 1. 후보 필터 */
        c.setAgeMaxDiff      (getInt(m, "age_max_diff",       c.getAgeMaxDiff()));
        c.setLastActiveDays  (getInt(m, "last_active_days",   c.getLastActiveDays()));
        c.setPassCooldownDays(getInt(m, "pass_cooldown_days", c.getPassCooldownDays()));

        /* 2. 풀 */
        c.setPoolSize        (getInt   (m, "pool_size",          c.getPoolSize()));
        c.setNewbieRatio     (getDouble(m, "newbie_ratio",       c.getNewbieRatio()));
        c.setNewbieWindowDays(getInt   (m, "newbie_window_days", c.getNewbieWindowDays()));
        c.setRadiusStepsKm   (getIntArr(m, "radius_steps_km",    c.getRadiusStepsKm()));

        /* 3. 키워드 */
        c.setKeywordBase        (getDouble(m, "keyword_base",          c.getKeywordBase()));
        c.setKeywordCoef        (getDouble(m, "keyword_coef",          c.getKeywordCoef()));
        c.setKeywordTagThreshold(getDouble(m, "keyword_tag_threshold", c.getKeywordTagThreshold()));

        /* 4. 이상형 */
        c.setIdealBothMin    (getDouble(m, "ideal_both_min",     c.getIdealBothMin()));
        c.setILikeThreshold  (getDouble(m, "i_like_threshold",   c.getILikeThreshold()));
        c.setLikesMeThreshold(getDouble(m, "likes_me_threshold", c.getLikesMeThreshold()));
        c.setIdealMinFields  (getInt   (m, "ideal_min_fields",   c.getIdealMinFields()));
        c.setIdealFewPenalty (getDouble(m, "ideal_few_penalty",  c.getIdealFewPenalty()));

        /* 5. 라이프 */
        c.setLifestyleTagThreshold(getDouble(m, "lifestyle_tag_threshold", c.getLifestyleTagThreshold()));

        /* 6. 지역 */
        c.setRegionTiers        (getTiers (m, "region_tiers",           c.getRegionTiers()));
        c.setLocationTagThreshold(getDouble(m, "location_tag_threshold", c.getLocationTagThreshold()));

        /* 7. sortKey 가중치 + 중요포인트 차등 가산 */
        c.setWeightKeyword  (getDouble(m, "weight_keyword",   c.getWeightKeyword()));
        c.setWeightIdeal    (getDouble(m, "weight_ideal",     c.getWeightIdeal()));
        c.setWeightLifestyle(getDouble(m, "weight_lifestyle", c.getWeightLifestyle()));
        c.setWeightLocation (getDouble(m, "weight_location",  c.getWeightLocation()));
        c.setBumpKeyword    (getDouble(m, "bump_keyword",     c.getBumpKeyword()));
        c.setBumpIdeal      (getDouble(m, "bump_ideal",       c.getBumpIdeal()));
        c.setBumpLifestyle  (getDouble(m, "bump_lifestyle",   c.getBumpLifestyle()));
        c.setBumpLocation   (getDouble(m, "bump_location",    c.getBumpLocation()));

        /* 8. 같은카테고리 */
        c.setCategoryMateCats(getStrList(m, "category_mate_cats", c.getCategoryMateCats()));
        c.setCategoryMateMin (getInt    (m, "category_mate_min",  c.getCategoryMateMin()));

        /* 9. 개인키워드 */
        c.setCustomKwMin(getInt(m, "custom_kw_min", c.getCustomKwMin()));

        /* 10. 슬롯 */
        c.setDailyShow  (getInt(m, "daily_show",   c.getDailyShow()));
        c.setNewbieFloor(getInt(m, "newbie_floor", c.getNewbieFloor()));
        c.setRandomSlots(getInt(m, "random_slots", c.getRandomSlots()));

        /* 11. 표시 */
        c.setKeywordChipCount(getInt(m, "keyword_chip_count", c.getKeywordChipCount()));

        /* 12. 신규자 fan-out (매시간 미니 배치) */
        c.setFreshNewbieWindowHours  (getInt(m, "fresh_newbie_window_hours",   c.getFreshNewbieWindowHours()));
        c.setFreshNewbieFanOut       (getInt(m, "fresh_newbie_fan_out",        c.getFreshNewbieFanOut()));
        c.setFreshNewbieReservedSlots(getInt(m, "fresh_newbie_reserved_slots", c.getFreshNewbieReservedSlots()));
        c.setFreshNewbieReservedStep (getInt(m, "fresh_newbie_reserved_step",  c.getFreshNewbieReservedStep()));

        /* §10.22 ProfileChangeListener 어뷰즈 가드 */
        c.setRecomputeActionThreshold(getInt(m, "recompute_action_threshold", c.getRecomputeActionThreshold()));
        c.setRecomputeMaxPerDay      (getInt(m, "recompute_max_per_day",       c.getRecomputeMaxPerDay()));

        return c;
    }

    /* ─── 파서 헬퍼 — 파싱 실패 시 폴백 + warn ─── */

    private int getInt(Map<String, String> m, String key, int fallback) {
        String raw = m.get(key);
        if (raw == null) return fallback;
        try { return Integer.parseInt(raw); }
        catch (NumberFormatException e) {
            log.warn("[MatchingConfig] INT 파싱 실패 — key={}, value={}, fallback={}", key, raw, fallback);
            return fallback;
        }
    }

    private double getDouble(Map<String, String> m, String key, double fallback) {
        String raw = m.get(key);
        if (raw == null) return fallback;
        try { return Double.parseDouble(raw); }
        catch (NumberFormatException e) {
            log.warn("[MatchingConfig] DOUBLE 파싱 실패 — key={}, value={}, fallback={}", key, raw, fallback);
            return fallback;
        }
    }

    private int[] getIntArr(Map<String, String> m, String key, int[] fallback) {
        String raw = m.get(key);
        if (raw == null) return fallback;
        try { return OM.readValue(raw, int[].class); }
        catch (Exception e) {
            log.warn("[MatchingConfig] JSON int[] 파싱 실패 — key={}, value={}", key, raw);
            return fallback;
        }
    }

    private double[][] getTiers(Map<String, String> m, String key, double[][] fallback) {
        String raw = m.get(key);
        if (raw == null) return fallback;
        try { return OM.readValue(raw, double[][].class); }
        catch (Exception e) {
            log.warn("[MatchingConfig] JSON double[][] 파싱 실패 — key={}, value={}", key, raw);
            return fallback;
        }
    }

    private List<String> getStrList(Map<String, String> m, String key, List<String> fallback) {
        String raw = m.get(key);
        if (raw == null) return fallback;
        try { return OM.readValue(raw, new TypeReference<List<String>>() {}); }
        catch (Exception e) {
            log.warn("[MatchingConfig] JSON List<String> 파싱 실패 — key={}, value={}", key, raw);
            return fallback;
        }
    }
}
