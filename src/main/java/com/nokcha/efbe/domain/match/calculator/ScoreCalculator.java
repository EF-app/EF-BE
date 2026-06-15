package com.nokcha.efbe.domain.match.calculator;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.Ideal;
import com.nokcha.efbe.domain.match.model.MatchUtil;
import com.nokcha.efbe.domain.match.model.Self;
import com.nokcha.efbe.domain.match.model.StyleScore;
import com.nokcha.efbe.domain.match.model.Tendency;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.pool.GeoUtil;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 4 영역 점수 산식 통합
 *  메서드 4개 = 키워드 / 이상형 / 라이프 / 지역.
 *
 *  ── 산식 개요 ────────────────────────────
 *    keyword   : base + Jaccard(keywords ∪ customKeywords) × coef
 *    ideal     : 6 필드 (hair / body / height / tendency / fashion / grooming) 양방향 평가
 *    lifestyle : (drinkScore + smokeScore) / 2
 *    location  : Haversine → 5 단계 구간 점수
 *
 *  ※ chips (희귀 키워드 정렬) 는 {@link MatchCalculator} 의 책임 — 점수 계산과 분리.
 */
@Component
public class ScoreCalculator {

    /* ───  키워드 ─────────────────────────────────────────────────────────
     *  keywordScore = base + Jaccard(keywords ∪ customKeywords) × coef
     *  - 한쪽이라도 비어있으면 base 반환 (안 겹쳐도 극단 불이익 없음)
     */
    public double keyword(UserContext a, UserContext b, MatchingConfig cfg) {
        Set<String> allA = MatchUtil.union(a.keywords(), a.customKeywords());
        Set<String> allB = MatchUtil.union(b.keywords(), b.customKeywords());
        if (allA.isEmpty() || allB.isEmpty()) return cfg.getKeywordBase();
        return cfg.getKeywordBase() + MatchUtil.jaccard(allA, allB) * cfg.getKeywordCoef();
    }

    /* ─── 이상형 — 양방향 평가 ───────────────────────────────────────────
     *  6 필드 평가 → aToB / bToA / bidir 반환 (StyleScore record).
     *  - "상관없음" 필드는 평가 스킵, n 카운트 미포함
     *  - n == 0 → 0.5 중립
     *  - n < idealMinFields → ×idealFewPenalty 감점
     */
    public StyleScore ideal(UserContext me, UserContext other, MatchingConfig cfg) {
        boolean meHas    = me.ideal().hasAnyField();
        boolean otherHas = other.ideal().hasAnyField();
        double aToB = evaluateIdeal(me.ideal(),    other.self(), cfg);
        double bToA = evaluateIdeal(other.ideal(), me.self(),    cfg);
        return new StyleScore((aToB + bToA) / 2.0, aToB, bToA, meHas, otherHas);
    }

    private double evaluateIdeal(Ideal ideal, Self self, MatchingConfig cfg) {
        int n = 0;
        double sum = 0.0;

        if (!ideal.isHairDontCare() && self.hair() != null) {
            n++;
            sum += MatchUtil.stepDistance(ideal.hair().ordinal(), self.hair().ordinal());
        }
        if (!ideal.isBodyDontCare() && self.body() != null) {
            n++;
            sum += MatchUtil.stepDistance(ideal.body().ordinal(), self.body().ordinal());
        }
        if (!ideal.isHeightDontCare() && self.height() != null) {
            n++;
            sum += MatchUtil.stepDistance(ideal.height().ordinal(), self.height().ordinal());
        }
        if (!ideal.isTendencyDontCare() && self.tendency() != null) {
            n++;
            sum += tendency(ideal.tendency(), self.tendency());
        }
        if (!ideal.isFashionDontCare() && self.fashion() != null) {
            n++;
            sum += MatchUtil.jaccard(ideal.fashion(), self.fashion());
        }
        if (!ideal.isGroomingDontCare() && self.grooming() != null) {
            n++;
            sum += ideal.grooming() == self.grooming() ? 1.0 : 0.3;
        }

        if (n == 0) return 0.5;  // 전부 미입력 → 중립
        double avg = sum / n;
        if (n < cfg.getIdealMinFields()) avg *= cfg.getIdealFewPenalty();
        return avg;
    }

    /**
     * Tendency 매트릭스
     *  스펙트럼 4 (ON_GIP/GIP_PREF/TXT_PREF/ON_TXT) + 특수 1 (PLATONIC).
     */
    private double tendency(Tendency idealVal, Tendency selfVal) {
        if (idealVal == Tendency.PLATONIC) {
            return selfVal == Tendency.PLATONIC ? 1.0 : 0.1;
        }
        if (selfVal == Tendency.PLATONIC) return 0.1;

        int d = Math.abs(idealVal.ordinal() - selfVal.ordinal());
        if (d == 0) {
            return (idealVal == Tendency.ON_GIP || idealVal == Tendency.ON_TXT) ? 0.0 : 0.3;
        }
        if (d == 1) return 0.2;
        if (d == 2) return 0.9;
        return 1.0;  // d == 3 (양 끝단)
    }

    /* ─── 라이프 — 음주·흡연 평균 ────────────────────────────────────────
     */
    public double lifestyle(UserContext a, UserContext b) {
        double drink = MatchUtil.stepDistance(a.drinking().idx(), b.drinking().idx());
        double smoke = MatchUtil.stepDistance(a.smoking().idx(),  b.smoking().idx());
        return (drink + smoke) / 2.0;
    }

    /* ─── 지역 — Haversine 5 단계 ────────────────────────────────────────
     */
    public double location(UserContext a, UserContext b, MatchingConfig cfg) {
        double km = GeoUtil.haversine(a.lat(), a.lon(), b.lat(), b.lon());
        for (double[] tier : cfg.getRegionTiers()) {
            if (km < tier[0]) return tier[1];
        }
        return 0.2;  // 폴백 — regionTiers 가 비정상이거나 99999 이상
    }
}
