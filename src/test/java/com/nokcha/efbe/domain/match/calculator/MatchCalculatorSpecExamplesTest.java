package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 계산 예시 검증.
 *  - 키워드 0.55
 *  - 라이프 0.80
 *  - 지역 26.8km → 0.6, 140.7km → 0.2
 *  - sortKey 0.66
 */
class MatchCalculatorSpecExamplesTest {

    private static final double EPS = 0.001;

    private final ScoreCalculator scoreCalc       = new ScoreCalculator();
    private final SortKeyCalculator sortKeyCalc   = new SortKeyCalculator();

    private MatchingConfig cfg() {
        // 코드 기본값 = 폴백 그대로 사용.
        return new MatchingConfig();
    }

    @Nested
    @DisplayName("키워드 0.55")
    class KeywordExample {

        @Test
        @DisplayName("A 7개 × B 3개 교집합 2 → Jaccard 0.25 → 0.4 + 0.25×0.6 = 0.55")
        void example() {
            UserContext a = userWithKeywords(Set.of("락","K-POP","힙합","독서","사진","영화","재즈"));
            UserContext b = userWithKeywords(Set.of("사진","OST","영화"));

            double score = scoreCalc.keyword(a, b, cfg());

            assertEquals(0.55, score, EPS);
        }

        private UserContext userWithKeywords(Set<String> keywords) {
            return baseUserBuilder()
                    .withKeywords(keywords)
                    .build();
        }
    }

    @Nested
    @DisplayName("라이프 0.80")
    class LifestyleExample {

        @Test
        @DisplayName("음주 RARE↔MODERATE(차1=0.6) + 흡연 NEVER↔NEVER(차0=1.0) → 0.80")
        void example() {
            UserContext a = baseUserBuilder()
                    .withDrinking(Drinking.RARE)
                    .withSmoking(Smoking.NEVER)
                    .build();
            UserContext b = baseUserBuilder()
                    .withDrinking(Drinking.MODERATE)
                    .withSmoking(Smoking.NEVER)
                    .build();

            double score = scoreCalc.lifestyle(a, b);

            assertEquals(0.80, score, EPS);
        }
    }

    @Nested
    @DisplayName("지역 — Haversine 5단계")
    class LocationExample {

        @Test
        @DisplayName("마포구(37.5663,126.9019) ↔ 인천 중구(37.4738,126.6216) ≈ 26.8km → 0.6")
        void mapoToIncheon() {
            UserContext a = baseUserBuilder().withCoord(37.5663, 126.9019).build();
            UserContext b = baseUserBuilder().withCoord(37.4738, 126.6216).build();

            double score = scoreCalc.location(a, b, cfg());

            // 20~50km 구간 → 0.6
            assertEquals(0.6, score, 0.0001);
        }

        @Test
        @DisplayName("광주 동구(35.1465,126.9230) ↔ 대전 유성구(36.3622,127.3562) ≈ 140.7km → 0.2")
        void gwangjuToDaejeon() {
            UserContext a = baseUserBuilder().withCoord(35.1465, 126.9230).build();
            UserContext b = baseUserBuilder().withCoord(36.3622, 127.3562).build();

            double score = scoreCalc.location(a, b, cfg());

            // 100km 초과 → 0.2
            assertEquals(0.2, score, 0.0001);
        }
    }

    @Nested
    @DisplayName("sortKey 0.66")
    class SortKeyExample {

        @Test
        @DisplayName("keyword 0.55, ideal 0.60, lifestyle 0.80, location 1.0, 중요포인트 없음 → 0.66")
        void example() {
            UserContext me = baseUserBuilder().build();  // importantPoints 비어있음

            double sortKey = sortKeyCalc.calc(me, 0.55, 0.60, 0.80, 1.0, cfg());

            // 0.55*0.40 + 0.60*0.35 + 0.80*0.10 + 1.0*0.15 = 0.66
            assertEquals(0.66, sortKey, EPS);
        }

        @Test
        @DisplayName("LOCATION 중요포인트 → 지역 가중치 +0.05 (bumpLocation) → 정규화 후 sortKey 상승")
        void withLocationImportant() {
            UserContext meDefault = baseUserBuilder().build();
            UserContext meLocImportant = baseUserBuilder()
                    .withImportantPoints(Set.of(IdealPointType.AREA))
                    .build();
            MatchingConfig cfg = cfg();

            double s0 = sortKeyCalc.calc(meDefault,     0.55, 0.60, 0.80, 1.0, cfg);
            double s1 = sortKeyCalc.calc(meLocImportant, 0.55, 0.60, 0.80, 1.0, cfg);

            // loc=1.0 이라 LOCATION 중요로 지정 시 sortKey 상승해야 함
            assertTrue(s1 > s0, "LOCATION 중요포인트 적용 시 sortKey 상승해야: s0=" + s0 + ", s1=" + s1);
        }

        @Test
        @DisplayName("차등 가산 — IDEAL 가산(0.20) > KEYWORD(0.15) > LIFESTYLE/LOCATION(0.05)")
        void bumpHierarchy() {
            MatchingConfig cfg = cfg();
            // 점수 분포가 동일할 때, IDEAL 중요 vs LIFESTYLE 중요 비교
            UserContext meIdeal     = baseUserBuilder().withImportantPoints(
                    Set.of(IdealPointType.IDEAL_TYPE)).build();
            UserContext meLifestyle = baseUserBuilder().withImportantPoints(
                    Set.of(IdealPointType.LIFE_STYLE)).build();

            // ideal 점수 영역만 높이면 IDEAL 중요인 사람이 더 큰 sortKey 받아야
            double sIdeal     = sortKeyCalc.calc(meIdeal,     0.50, 0.90, 0.50, 0.50, cfg);
            double sLifestyle = sortKeyCalc.calc(meLifestyle, 0.50, 0.90, 0.50, 0.50, cfg);

            assertTrue(sIdeal > sLifestyle,
                    "IDEAL bump(0.20) > LIFESTYLE bump(0.05) — 같은 점수면 IDEAL 중요가 sortKey 더 높아야: " +
                            "ideal=" + sIdeal + ", lifestyle=" + sLifestyle);
        }
    }

    /* ─── 테스트용 UserContext 빌더 — record 라 별도 빌더 직접 작성 ─── */

    private static TestUser baseUserBuilder() {
        return new TestUser();
    }

    private static final class TestUser {
        private long id = 1L;
        private int age = 27;
        private LocalDate signupAt = LocalDate.now().minusDays(30);
        private String regionCountry = "한국";
        private double lat = 37.5;
        private double lon = 127.0;
        private Purpose purpose
                = Purpose.MIXED;
        private Set<String> keywords = Set.of();
        private Set<String> customKeywords = Set.of();
        private Map<String, Set<String>> keywordsByCategory = Map.of();
        private Ideal ideal = new Ideal(null, null, null, null, null, null);
        private Self self = new Self(null, null, null, null, null, null);
        private Drinking drinking = Drinking.NEVER;
        private Smoking smoking = Smoking.NEVER;
        private Set<IdealPointType> importantPoints = Set.of();

        TestUser withKeywords(Set<String> v) { this.keywords = v; return this; }
        TestUser withDrinking(Drinking v) { this.drinking = v; return this; }
        TestUser withSmoking(Smoking v) { this.smoking = v; return this; }
        TestUser withCoord(double la, double lo) { this.lat = la; this.lon = lo; return this; }
        TestUser withImportantPoints(Set<IdealPointType> v) {
            this.importantPoints = v; return this;
        }

        UserContext build() {
            return new UserContext(
                    id, age, signupAt, regionCountry, lat, lon, purpose,
                    keywords, customKeywords, keywordsByCategory,
                    ideal, self, drinking, smoking, importantPoints
            );
        }
    }
}
