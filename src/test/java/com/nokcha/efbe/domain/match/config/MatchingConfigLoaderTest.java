package com.nokcha.efbe.domain.match.config;

import com.nokcha.efbe.domain.match.repository.MatchConfigSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 체크리스트:
 *  - code_match_config 로드 → MatchingConfig 매핑
 *  - 키 누락 시 코드 기본값 폴백
 *  - JSON 값 (radius_steps_km / region_tiers / category_mate_cats) 파싱
 *  - 잘못된 값 → warn 로그 + 폴백 (배치 정지 안 됨)
 */
class MatchingConfigLoaderTest {

    private MatchingConfigLoader newLoader(Map<String, String> data) {
        MatchConfigSource stub = () -> data;
        return new MatchingConfigLoader(stub);
    }

    @Test
    @DisplayName("키 누락 시 코드 기본값 폴백")
    void fallbackToCodeDefaults() {
        MatchingConfig defaults = new MatchingConfig();
        MatchingConfig loaded = newLoader(Map.of()).load();

        // 주요 키 몇 개만 일치 확인 — 폴백이 모든 키에 적용
        assertEquals(defaults.getPoolSize(),      loaded.getPoolSize());
        assertEquals(defaults.getDailyShow(),     loaded.getDailyShow());
        assertEquals(defaults.getKeywordBase(),   loaded.getKeywordBase());
        assertEquals(defaults.getWeightKeyword(), loaded.getWeightKeyword());
        assertEquals(defaults.getBumpKeyword(),   loaded.getBumpKeyword());
        assertEquals(defaults.getBumpIdeal(),     loaded.getBumpIdeal());
        assertEquals(defaults.getBumpLifestyle(), loaded.getBumpLifestyle());
        assertEquals(defaults.getBumpLocation(),  loaded.getBumpLocation());
        assertArrayEquals(defaults.getRadiusStepsKm(), loaded.getRadiusStepsKm());
    }

    @Test
    @DisplayName("INT / DOUBLE 키 정상 로드")
    void loadScalarKeys() {
        Map<String, String> data = Map.of(
                "pool_size",       "300",
                "daily_show",      "30",
                "keyword_base",    "0.50",
                "weight_keyword",  "0.45",
                "bump_ideal",      "0.25"
        );
        MatchingConfig loaded = newLoader(data).load();

        assertEquals(300,  loaded.getPoolSize());
        assertEquals(30,   loaded.getDailyShow());
        assertEquals(0.50, loaded.getKeywordBase());
        assertEquals(0.45, loaded.getWeightKeyword());
        assertEquals(0.25, loaded.getBumpIdeal());
    }

    @Test
    @DisplayName("JSON 키 — radius_steps_km int[] 파싱")
    void loadJsonIntArray() {
        MatchingConfig loaded = newLoader(Map.of("radius_steps_km", "[10,30,80,-1]")).load();
        assertArrayEquals(new int[]{10, 30, 80, -1}, loaded.getRadiusStepsKm());
    }

    @Test
    @DisplayName("JSON 키 — region_tiers double[][] 파싱")
    void loadJsonNestedArray() {
        MatchingConfig loaded = newLoader(Map.of(
                "region_tiers", "[[3,1.0],[10,0.8],[30,0.5],[99999,0.2]]")).load();

        double[][] tiers = loaded.getRegionTiers();
        assertEquals(4, tiers.length);
        assertEquals(3.0, tiers[0][0]);
        assertEquals(1.0, tiers[0][1]);
        assertEquals(99999.0, tiers[3][0]);
    }

    @Test
    @DisplayName("JSON 키 — category_mate_cats List<String> 파싱")
    void loadJsonStringList() {
        MatchingConfig loaded = newLoader(Map.of(
                "category_mate_cats", "[\"OUTDOOR\",\"SELF_DEV\"]")).load();

        List<String> cats = loaded.getCategoryMateCats();
        assertEquals(2, cats.size());
        assertEquals("OUTDOOR", cats.get(0));
        assertEquals("SELF_DEV", cats.get(1));
    }

    @Test
    @DisplayName("잘못된 INT 값 → 폴백 (코드 기본값) — 배치 정지 안 함")
    void invalidIntFallsBack() {
        MatchingConfig defaults = new MatchingConfig();
        MatchingConfig loaded = newLoader(Map.of("pool_size", "not-a-number")).load();

        assertEquals(defaults.getPoolSize(), loaded.getPoolSize());
    }

    @Test
    @DisplayName("잘못된 JSON → 폴백 (코드 기본값)")
    void invalidJsonFallsBack() {
        MatchingConfig defaults = new MatchingConfig();
        MatchingConfig loaded = newLoader(Map.of(
                "radius_steps_km", "[1, 2,,]",       // 잘못된 JSON
                "region_tiers",    "not json at all"
        )).load();

        assertArrayEquals(defaults.getRadiusStepsKm(), loaded.getRadiusStepsKm());
        assertNotNull(loaded.getRegionTiers());
        assertEquals(defaults.getRegionTiers().length, loaded.getRegionTiers().length);
    }
}
