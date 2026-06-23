package com.nokcha.efbe.domain.match.model;

/**
 * 키 구간 — code_personal "키" 카테고리 매핑.
 *  선언 순서 = 단계 (낮은 키부터). "선택 안함" 제외.
 */
public enum HeightBand {
    UNDER_150,   // 150이하
    H_151_155,   // 151~155
    H_156_160,   // 156~160
    H_161_165,   // 160~165   (시드 표기 그대로)
    H_166_170,   // 166~170
    H_171_PLUS   // 171이상
}
