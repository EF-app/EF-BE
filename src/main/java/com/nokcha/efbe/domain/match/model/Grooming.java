package com.nokcha.efbe.domain.match.model;

/**
 * 꾸미는 스타일 — code_personal "꾸미는 스타일" 카테고리 매핑.
 * 매칭: 일치 1.0 / 불일치 0.3 (단계 거리 미사용 — 이진 비교).
 */
public enum Grooming {
    LIKE_GROOMING,  // 꾸미는 걸 좋아해요
    NATURAL,        // 자연스러운 꾸안꾸
    CLEAN,          // 깔끔하게 신경 써요
    COMFORTABLE,    // 편한 게 좋아요
    SITUATIONAL,    // 상황에 따라 달라요
    ETC             // 기타
}
