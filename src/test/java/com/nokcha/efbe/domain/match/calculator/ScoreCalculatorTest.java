package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.fixture.UserContextBuilder;
import com.nokcha.efbe.domain.match.model.BodyType;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.Fashion;
import com.nokcha.efbe.domain.match.model.Grooming;
import com.nokcha.efbe.domain.match.model.HairLength;
import com.nokcha.efbe.domain.match.model.HeightBand;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.match.model.StyleScore;
import com.nokcha.efbe.domain.match.model.Tag;
import com.nokcha.efbe.domain.match.model.Tendency;
import com.nokcha.efbe.domain.match.model.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 체크리스트 — 4 영역 점수 산식 검증.
 *  통합 calculator 의 메서드별 테스트를 @Nested 로 영역 격리.
 */
class ScoreCalculatorTest {

    private static final double EPS = 0.001;

    private final ScoreCalculator calc = new ScoreCalculator();
    private final MatchingConfig cfg = new MatchingConfig();

    /* ─── 키워드 ─────────────────────────────────────────────── */
    @Nested
    @DisplayName("키워드")
    class Keyword {

        @Test
        @DisplayName("한쪽이라도 키워드·커스텀 전부 비면 base(0.4) 반환")
        void emptyReturnsBase() {
            UserContext a = UserContextBuilder.builder().keywords(Set.of()).customKeywords(Set.of()).build();
            UserContext b = UserContextBuilder.builder().keywords(Set.of("락","K-POP")).build();

            assertEquals(0.4, calc.keyword(a, b, cfg), EPS);
        }

        @Test
        @DisplayName("Jaccard=1 (완전 일치) → 0.4 + 1.0×0.6 = 1.0 상한")
        void perfectMatchUpperBound() {
            Set<String> tags = Set.of("락","K-POP","힙합");
            UserContext a = UserContextBuilder.builder().keywords(tags).build();
            UserContext b = UserContextBuilder.builder().keywords(tags).build();

            assertEquals(1.0, calc.keyword(a, b, cfg), EPS);
        }

        @Test
        @DisplayName("Jaccard=0 (교집합 0, 양쪽 비어있지 않음) → 0.4 하한 + 0×0.6 = 0.4")
        void disjointLowerBound() {
            UserContext a = UserContextBuilder.builder().keywords(Set.of("락","K-POP")).build();
            UserContext b = UserContextBuilder.builder().keywords(Set.of("재즈","클래식")).build();

            assertEquals(0.4, calc.keyword(a, b, cfg), EPS);
        }

        @Test
        @DisplayName("키워드+커스텀 합집합으로 평가 — 커스텀만 겹쳐도 점수 상승")
        void customKeywordCountsToward() {
            UserContext a = UserContextBuilder.builder()
                    .keywords(Set.of("락"))
                    .customKeywords(Set.of("LP판"))
                    .build();
            UserContext b = UserContextBuilder.builder()
                    .keywords(Set.of("클래식"))
                    .customKeywords(Set.of("LP판"))
                    .build();

            // 합집합 {락,LP판,클래식} = 3, 교집합 {LP판} = 1 → Jaccard 1/3
            // 0.4 + (1.0/3) * 0.6 = 0.6
            assertEquals(0.6, calc.keyword(a, b, cfg), EPS);
        }
    }

    /* ─── 이상형 — 6 필드 양방향 ────────────────────────────── */
    @Nested
    @DisplayName("이상형 — 양방향 6필드")
    class IdealField {

        @Test
        @DisplayName("6 필드 전부 완벽 일치 → bidir 0.8833 (tendency d=0 중간단 0.3 보정)")
        void allSixFieldsPerfect() {
            Ideal ideal = UserContextBuilder.ideal(
                    HairLength.SHORT, BodyType.SLIM, HeightBand.H_166_170,
                    Tendency.GIP_PREF, Set.of(Fashion.CASUAL), Grooming.CLEAN);
            Self self = UserContextBuilder.self(
                    HairLength.SHORT, BodyType.SLIM, HeightBand.H_166_170,
                    Tendency.GIP_PREF, Set.of(Fashion.CASUAL), Grooming.CLEAN);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.8833, s.aToB(), EPS);
            assertEquals(0.8833, s.bToA(), EPS);
            assertEquals(0.8833, s.bidir(), EPS);
            assertTrue(s.aHasIdeal());
            assertTrue(s.bHasIdeal());
        }

        @Test
        @DisplayName("aToB != bToA — 한쪽 이상형이 비대칭일 때 다른 점수")
        void asymmetricStyleScore() {
            Ideal aIdeal = new Ideal(HairLength.SHORT, null, null, null, Set.of(), null);
            Self aSelf   = new Self(HairLength.LONG,   null, null, null, Set.of(), null);
            Ideal bIdeal = new Ideal(HairLength.LONG,  null, null, null, Set.of(), null);
            Self bSelf   = new Self(HairLength.LONG,   null, null, null, Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(aIdeal).self(aSelf).build();
            UserContext b = UserContextBuilder.builder().ideal(bIdeal).self(bSelf).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.3 * 0.8, s.aToB(), EPS);
            assertEquals(1.0 * 0.8, s.bToA(), EPS);
            assertEquals((s.aToB() + s.bToA()) / 2.0, s.bidir(), EPS);
        }

        @Test
        @DisplayName("이상형 전부 미입력 → 0.5 중립, hasAnyField=false")
        void noIdealReturnsNeutral() {
            UserContext a = UserContextBuilder.builder().ideal(UserContextBuilder.noIdeal()).build();
            UserContext b = UserContextBuilder.builder().ideal(UserContextBuilder.noIdeal()).build();

            StyleScore s = calc.ideal(a, b, cfg);

            assertEquals(0.5, s.aToB(), EPS);
            assertEquals(0.5, s.bToA(), EPS);
            assertFalse(s.aHasIdeal());
            assertFalse(s.bHasIdeal());
        }

        @Test
        @DisplayName("평가 필드 n=2 < 3 → idealFewPenalty 0.8 적용")
        void fewerThanMinFieldsPenalty() {
            Ideal ideal = new Ideal(HairLength.SHORT, BodyType.SLIM, null, null, Set.of(), null);
            Self self   = new Self(HairLength.SHORT,  BodyType.SLIM,  null, null, Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.8, s.aToB(), EPS);
        }

        @Test
        @DisplayName("평가 필드 n=3 이상 → 감점 없음")
        void atLeastMinFieldsNoPenalty() {
            Ideal ideal = new Ideal(HairLength.SHORT, BodyType.SLIM, HeightBand.H_166_170,
                    null, Set.of(), null);
            Self self   = new Self(HairLength.SHORT,  BodyType.SLIM,  HeightBand.H_166_170,
                    null, Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(1.0, s.aToB(), EPS);
        }

        @Test
        @DisplayName("'상관없음' (DontCare) 필드는 평가 스킵 — n 카운트 미포함")
        void dontCareFieldsSkipped() {
            Ideal ideal = new Ideal(HairLength.SHORT, null, null, null, Set.of(), null);
            Self self   = new Self(HairLength.MEDIUM, null, null, null, Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.48, s.aToB(), EPS);
        }

        @Test
        @DisplayName("Tendency 매트릭스 — d=3 (양 끝단) = 1.0")
        void tendencyMatrixOppositeEnds() {
            Ideal ideal = new Ideal(null, null, null, Tendency.ON_GIP, Set.of(), null);
            Self self   = new Self(null, null, null,  Tendency.ON_TXT, Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.8, s.aToB(), EPS);
        }

        @Test
        @DisplayName("Tendency 매트릭스 — PLATONIC ↔ 스펙트럼 = 0.1")
        void tendencyMatrixPlatonicVsSpectrum() {
            Ideal ideal = new Ideal(null, null, null, Tendency.PLATONIC, Set.of(), null);
            Self self   = new Self(null, null, null,  Tendency.ON_GIP,   Set.of(), null);

            UserContext a = UserContextBuilder.builder().ideal(ideal).self(self).build();
            UserContext b = UserContextBuilder.builder().ideal(ideal).self(self).build();

            StyleScore s = calc.ideal(a, b, cfg);
            assertEquals(0.1 * 0.8, s.aToB(), EPS);
        }
    }

    /* ─── 라이프 — 음주·흡연 평균 ────────────────────────────── */
    @Nested
    @DisplayName("라이프 — 음주·흡연 평균")
    class Lifestyle {

        @Test
        @DisplayName("NEVER ↔ QUIT 은 같은 그룹 (idx 0) → 1.0")
        void neverEqualsQuit() {
            UserContext a = UserContextBuilder.builder()
                    .drinking(Drinking.NEVER).smoking(Smoking.NEVER).build();
            UserContext b = UserContextBuilder.builder()
                    .drinking(Drinking.QUIT).smoking(Smoking.QUIT).build();

            assertEquals(1.0, calc.lifestyle(a, b), EPS);
        }

        @Test
        @DisplayName("음주·흡연 모두 d=3 (양 끝) → stepDistance 0.1 평균 0.1")
        void maxDistanceBoth() {
            UserContext a = UserContextBuilder.builder()
                    .drinking(Drinking.NEVER).smoking(Smoking.NEVER).build();
            UserContext b = UserContextBuilder.builder()
                    .drinking(Drinking.OFTEN).smoking(Smoking.REGULAR).build();

            assertEquals(0.1, calc.lifestyle(a, b), EPS);
        }

        @Test
        @DisplayName("타투 항목 미반영 — 음주·흡연 2 개만 평균")
        void onlyTwoFactors() {
            UserContext a = UserContextBuilder.builder()
                    .drinking(Drinking.NEVER).smoking(Smoking.NEVER).build();
            UserContext b = UserContextBuilder.builder()
                    .drinking(Drinking.NEVER).smoking(Smoking.SOMETIMES).build();

            assertEquals(0.65, calc.lifestyle(a, b), EPS);
        }

        @Test
        @DisplayName("음주 RARE↔MODERATE d=1 → 0.6, 흡연 동일 → 1.0 평균 0.80")
        void specExample() {
            UserContext a = UserContextBuilder.builder()
                    .drinking(Drinking.RARE).smoking(Smoking.NEVER).build();
            UserContext b = UserContextBuilder.builder()
                    .drinking(Drinking.MODERATE).smoking(Smoking.NEVER).build();

            assertEquals(0.80, calc.lifestyle(a, b), EPS);
        }
    }

    /* ─── 지역 — Haversine 5 단계 ────────────────────────────── */
    @Nested
    @DisplayName("지역 — Haversine 5단계")
    class Location {

        private final double LEPS = 0.0001;

        @Test
        @DisplayName("동일 좌표 → 0km < 5km → 1.0")
        void sameCoord() {
            UserContext a = UserContextBuilder.builder().coord(37.5, 127.0).build();
            UserContext b = UserContextBuilder.builder().coord(37.5, 127.0).build();
            assertEquals(1.0, calc.location(a, b, cfg), LEPS);
        }

        @Test
        @DisplayName("100km 초과 → 폴백 0.2 (마지막 tier)")
        void over100kmFallback() {
            UserContext a = UserContextBuilder.builder().coord(35.1465, 126.9230).build();
            UserContext b = UserContextBuilder.builder().coord(36.3622, 127.3562).build();
            assertEquals(0.2, calc.location(a, b, cfg), LEPS);
        }

        @Test
        @DisplayName("Tag.nearby() 는 % 표시 안 함 (hasPercent=false)")
        void nearbyTagHasNoPercent() {
            Tag t = Tag.nearby();
            assertFalse(t.hasPercent(), "#가까운지역 태그는 % 표시 없어야 함");
            assertEquals(0, t.percent(), "percent 필드는 0 (미사용)");
        }
    }
}
