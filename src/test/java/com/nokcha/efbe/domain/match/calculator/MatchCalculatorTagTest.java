package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.BodyType;
import com.nokcha.efbe.domain.match.model.Drinking;
import com.nokcha.efbe.domain.match.model.HairLength;
import com.nokcha.efbe.domain.match.model.HeightBand;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.match.model.Smoking;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.match.model.PairScore;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.Tag;
import com.nokcha.efbe.domain.match.model.TagType;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.fixture.UserContextBuilder;
import com.nokcha.efbe.domain.match.service.KeywordFreqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 체크리스트 (태그):
 *  - 같은카테고리 OUTDOOR/SELF_DEV/SPORTS 만 발동
 *  - 개인키워드 공통 시 ✨ (CUSTOM_KW 태그)
 *  - 정반대의매력: 태그 0 + 교집합 0
 *  - 지인만(FRIEND) → 이상형 계열 태그 제외
 *  - 이상형 임계값 0.45(양방향) / 0.65(비대칭)
 *  - 공통 키워드 칩: 빈도 낮은 것부터 N개
 */
class MatchCalculatorTagTest {

    private final MatchingConfig cfg = new MatchingConfig();

    /** 빈도 stub — 키워드별 임의 빈도 주입. 적게 등록된 게 더 희귀. */
    private final KeywordFreqService freqStub = new KeywordFreqService() {
        Map<String, Integer> table = Map.of(
                "락",   100,
                "재즈",  3,
                "사진",  50,
                "영화",  200,
                "독서",  20,
                "LP판",  2,
                "필름카메라", 5);
        @Override public int countOf(String keyword) { return table.getOrDefault(keyword, 0); }
        @Override public void refresh() { /* stub — 캐시 갱신 불필요 */ }
    };

    private MatchCalculator newCalculator() {
        return new MatchCalculator(
                new ScoreCalculator(),
                freqStub
        );
    }

    @Test
    @DisplayName("같은카테고리 — OUTDOOR/SELF_DEV/SPORTS 만 발동, 다른 카테고리는 안 됨")
    void categoryMateOnlyForThree() {
        // me, other 모두 MUSIC 카테고리에서 5개 공통이어도 — 태그 발동 X
        Map<String, Set<String>> meCats = Map.of(
                "MUSIC",  Set.of("락","재즈","K-POP","힙합","R&B"),
                "OUTDOOR", Set.of("등산","캠핑")
        );
        Map<String, Set<String>> otherCats = Map.of(
                "MUSIC",  Set.of("락","재즈","K-POP","힙합","R&B"),
                "OUTDOOR", Set.of("등산","캠핑")
        );
        UserContext me = UserContextBuilder.builder()
                .keywords(Set.of("락","재즈","K-POP","힙합","R&B","등산","캠핑"))
                .keywordsByCategory(meCats).build();
        UserContext other = UserContextBuilder.builder()
                .keywords(Set.of("락","재즈","K-POP","힙합","R&B","등산","캠핑"))
                .keywordsByCategory(otherCats).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        // CATEGORY_MATE 태그가 OUTDOOR 만 발동 (MUSIC 은 발동 X)
        long catCount = ps.tags().stream().filter(t -> t.type() == TagType.CATEGORY_MATE).count();
        assertEquals(1, catCount);
        assertTrue(ps.categoryMateCodes().contains("OUTDOOR"));
        assertFalse(ps.categoryMateCodes().contains("MUSIC"));
    }

    @Test
    @DisplayName("개인키워드 공통 ≥ 1 → CUSTOM_KW 태그 발동 (✨)")
    void customKeywordTagFires() {
        UserContext me = UserContextBuilder.builder()
                .customKeywords(Set.of("LP판","필름카메라")).build();
        UserContext other = UserContextBuilder.builder()
                .customKeywords(Set.of("LP판")).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        Tag customKw = ps.tags().stream().filter(t -> t.type() == TagType.CUSTOM_KW).findFirst().orElseThrow();
        assertTrue(customKw.chips().contains("LP판"));
        assertTrue(ps.hasCustomKeyword());
    }

    @Test
    @DisplayName("정반대의매력 — 7 태그 모두 미발동 + 관심사∩=0 + 커스텀∩=0")
    void totalOppositeTriggers() {
        // 관심사 disjoint, 커스텀 disjoint, 이상형 없음, 거리 멀고, 라이프 d=3
        UserContext me = UserContextBuilder.builder()
                .keywords(Set.of("락","K-POP","힙합"))
                .coord(35.1465, 126.9230)  // 광주
                .drinking(Drinking.NEVER)
                .smoking(Smoking.NEVER)
                .build();
        UserContext other = UserContextBuilder.builder()
                .keywords(Set.of("재즈","클래식","트로트"))
                .coord(36.3622, 127.3562)  // 대전
                .drinking(Drinking.OFTEN)
                .smoking(Smoking.REGULAR)
                .build();

        PairScore ps = newCalculator().score(me, other, cfg);

        assertTrue(ps.hasTotalOpposite(), "정반대의매력 발동해야: " + ps.tags());
        assertTrue(ps.tags().stream().anyMatch(t -> t.type() == TagType.TOTAL_OPPOSITE));
    }

    @Test
    @DisplayName("FRIEND 가드 — 한쪽이라도 FRIEND 면 IDEAL / I_LIKE / LIKES_ME 전부 제외")
    void friendGuardSkipsIdealTags() {
        // 양쪽 이상형 1.0 (양방향 임계값 0.45 통과 + 비대칭 0.65 통과)
        Ideal idealAll = new Ideal(HairLength.SHORT, BodyType.SLIM,
                HeightBand.H_166_170, null, Set.of(), null);
        Self selfAll = new Self(HairLength.SHORT, BodyType.SLIM,
                HeightBand.H_166_170, null, Set.of(), null);

        UserContext me = UserContextBuilder.builder()
                .purpose(Purpose.FRIEND)
                .ideal(idealAll).self(selfAll).build();
        UserContext other = UserContextBuilder.builder()
                .purpose(Purpose.LOVE)
                .ideal(idealAll).self(selfAll).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.IDEAL),
                "IDEAL 발동되면 안 됨");
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.I_LIKE),
                "I_LIKE 발동되면 안 됨");
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.LIKES_ME),
                "LIKES_ME 발동되면 안 됨");
    }

    @Test
    @DisplayName("이상형 임계값 — 양방향 0.45 미만이면 IDEAL 발동 X")
    void idealBothMinThreshold() {
        // n=1 필드만 일치 → 0.6 * 0.8 (penalty) = 0.48, 양방향 같음. ≥ 0.45 통과
        // n=1 일치인데 hair d=1 → 0.6 * 0.8 = 0.48, ≥ 0.45 통과
        // 더 명확한 케이스: aToB = 0.4 < 0.45 → IDEAL 미발동
        Ideal ideal = new Ideal(HairLength.SHORT, null, null, null, Set.of(), null);
        Self self   = new Self(HairLength.LONG,   null, null, null, Set.of(), null);  // d=2 → 0.3 * 0.8 = 0.24

        UserContext me = UserContextBuilder.builder().ideal(ideal).self(self).build();
        UserContext other = UserContextBuilder.builder().ideal(ideal).self(self).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        // 0.24 < 0.45 → IDEAL 미발동, I_LIKE/LIKES_ME 도 미발동 (0.24 < 0.65)
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.IDEAL));
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.I_LIKE));
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.LIKES_ME));
    }

    @Test
    @DisplayName("이상형 임계값 — 비대칭 aToB 0.65 이상 + bToA 미만 → I_LIKE 만")
    void idealAsymmetricILike() {
        // me 이상형 hair=SHORT, other self hair=SHORT → d=0 → 1.0 (n=1 * 0.8 penalty = 0.8)
        // other 이상형 hair=SHORT, me self hair=LONG → d=2 → 0.3 (penalty 0.8 = 0.24)
        // aToB=0.8 ≥ 0.65, bToA=0.24 < 0.65 → I_LIKE 만 발동
        Ideal meIdeal = new Ideal(HairLength.SHORT, null, null, null, Set.of(), null);
        Self meSelf   = new Self(HairLength.LONG,    null, null, null, Set.of(), null);
        Ideal otherIdeal = new Ideal(HairLength.SHORT, null, null, null, Set.of(), null);
        Self otherSelf   = new Self(HairLength.SHORT, null, null, null, Set.of(), null);

        UserContext me = UserContextBuilder.builder().ideal(meIdeal).self(meSelf).build();
        UserContext other = UserContextBuilder.builder().ideal(otherIdeal).self(otherSelf).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        assertTrue(ps.tags().stream().anyMatch(t -> t.type() == TagType.I_LIKE),
                "I_LIKE 발동되어야 함, 실제 태그: " + ps.tags());
        assertFalse(ps.tags().stream().anyMatch(t -> t.type() == TagType.LIKES_ME));
    }

    @Test
    @DisplayName("공통 키워드 칩 — 빈도 낮은 것부터 N개 (희귀한 것 우선)")
    void chipsOrderedByRarity() {
        // 모두 공통 — interests 5 개 (희귀도: LP판=2, 재즈=3, 필름카메라=5, 독서=20, 영화=200)
        Set<String> common = Set.of("LP판","재즈","필름카메라","독서","영화");
        UserContext me = UserContextBuilder.builder().keywords(common).build();
        UserContext other = UserContextBuilder.builder().keywords(common).build();

        PairScore ps = newCalculator().score(me, other, cfg);

        Tag keywordTag = ps.tags().stream()
                .filter(t -> t.type() == TagType.KEYWORD).findFirst().orElseThrow();
        List<String> chips = keywordTag.chips();
        // keywordChipCount 기본 3 → 가장 희귀한 3 개 (LP판, 재즈, 필름카메라)
        assertEquals(3, chips.size());
        assertEquals("LP판", chips.get(0));
        assertEquals("재즈", chips.get(1));
        assertEquals("필름카메라", chips.get(2));
    }
}
