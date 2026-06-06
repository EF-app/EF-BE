package com.nokcha.efbe.domain.match.model;

/**
 * 이상형 양방향 점수 (명세서 §2.2).
 *
 * @param bidir      (aToB + bToA) / 2 — #이상형 태그 표시값 (대칭)
 * @param aToB       A 이상형 ↔ B 실제 — #내가좋아하는 비대칭 판정
 * @param bToA       B 이상형 ↔ A 실제 — #나를좋아하는 비대칭 판정
 * @param aHasIdeal  A 가 이상형 1 필드라도 입력했나? (태그 가드)
 * @param bHasIdeal  B 가 이상형 1 필드라도 입력했나? (태그 가드)
 */
public record StyleScore(
        double bidir,
        double aToB,
        double bToA,
        boolean aHasIdeal,
        boolean bHasIdeal
) {}
