package com.nokcha.efbe.domain.match.model;

/**
 * 머리 길이 — code_personal "머리" 카테고리 매핑.
 *  선언 순서 = 단계. {@code stepDistance(ordinal_ideal, ordinal_self)} 로 거리 점수 산출.
 *  "선택 안함" 은 도메인에서 제외 (이상형에서는 DontCare → 평가 스킵).
 */
public enum HairLength {
    SHORT,    // 숏컷
    MEDIUM,   // 단발~중단발
    LONG      // 긴머리
}
