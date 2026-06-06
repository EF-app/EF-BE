package com.nokcha.efbe.domain.match.model;

/**
 * 흡연 — code_personal "흡연" 카테고리 매핑.
 *  명세서 §2.3: NEVER 와 QUIT 은 같은 idx (0).
 */
public enum Smoking {
    NEVER,      // 비흡연자
    QUIT,       // 금연 중
    RARE,       // 아주 가끔 피움
    SOMETIMES,  // 때때로 피움
    REGULAR;    // 흡연자

    public int idx() {
        return switch (this) {
            case NEVER, QUIT -> 0;
            case RARE -> 1;
            case SOMETIMES -> 2;
            case REGULAR -> 3;
        };
    }
}
