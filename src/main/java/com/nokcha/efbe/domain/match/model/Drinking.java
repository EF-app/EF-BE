package com.nokcha.efbe.domain.match.model;

/**
 * 음주 — code_personal "음주" 카테고리 매핑.
 * NEVER 와 QUIT 은 같은 idx (0). 나머지는 단계.
 */
public enum Drinking {
    NEVER,     // 아예 안 마심
    QUIT,      // 금주 중
    RARE,      // 가끔 마심
    MODERATE,  // 꽤 마심
    OFTEN;     // 자주 마심

    /** stepDistance 용 idx — NEVER=QUIT=0 같은 그룹. */
    public int idx() {
        return switch (this) {
            case NEVER, QUIT -> 0;
            case RARE -> 1;
            case MODERATE -> 2;
            case OFTEN -> 3;
        };
    }
}
