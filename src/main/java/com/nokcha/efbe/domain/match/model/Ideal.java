package com.nokcha.efbe.domain.match.model;

import java.util.Set;

/**
 * 이상형 — null / 빈 Set = "상관없음" (DontCare)
 *  fashion 은 다중 선택, 나머지는 단일
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

    /** 한 필드라도 입력했나? */
    public boolean hasAnyField() {
        return !isHairDontCare() || !isBodyDontCare() || !isHeightDontCare()
            || !isTendencyDontCare() || !isFashionDontCare() || !isGroomingDontCare();
    }
}
