package com.nokcha.efbe.domain.match.model;

/**
 * 패션 스타일 — code_personal "패션 스타일" 카테고리 매핑.
 *  명세서 §2.2 매칭: 다중 선택 → Set 으로 보관. Jaccard 유사도.
 */
public enum Fashion {
    CASUAL,    // 캐주얼
    STREET,    // 스트릿
    MINIMAL,   // 미니멀
    DANDY,     // 댄디
    SPORTY,    // 스포티
    VINTAGE,   // 빈티지
    ETC        // 기타
}
