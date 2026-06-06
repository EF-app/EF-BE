package com.nokcha.efbe.domain.match.model;

import java.util.Set;

/**
 * 이상형 — null / 빈 Set = "상관없음" (DontCare). 명세서 §2.2.
 *  fashion 은 다중 선택, 나머지는 단일 (v1.0 명세서 기준 — v12.1 의 다중 Set 채택 안 함).
 *  evaluateIdeal 에서 isDontCare 한 필드는 평가 스킵, n 카운트 미포함.
 */
public record Ideal(
        HairLength hair,
        BodyType body,
        HeightBand height,
        Tendency tendency,
        Set<Fashion> fashion,
        Grooming grooming
) {
    public boolean isHairDontCare()     { return hair == null; }
    public boolean isBodyDontCare()     { return body == null; }
    public boolean isHeightDontCare()   { return height == null; }
    public boolean isTendencyDontCare() { return tendency == null; }
    public boolean isFashionDontCare()  { return fashion == null || fashion.isEmpty(); }
    public boolean isGroomingDontCare() { return grooming == null; }

    /** 한 필드라도 입력했나? (#이상형/#내가/#나를 가드용 — 명세서 §3.1 가드 절) */
    public boolean hasAnyField() {
        return !isHairDontCare() || !isBodyDontCare() || !isHeightDontCare()
            || !isTendencyDontCare() || !isFashionDontCare() || !isGroomingDontCare();
    }
}
