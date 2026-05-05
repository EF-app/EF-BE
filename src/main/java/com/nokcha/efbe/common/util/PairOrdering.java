package com.nokcha.efbe.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PairOrdering {   // 매칭 페어 정렬

    // userId 작은 순으로 정렬
    public static Long[] ordered(Long a, Long b) {
        if (a == null || b == null) throw new IllegalArgumentException("null user id");
        return a <= b ? new Long[]{a, b} : new Long[]{b, a};
    }

    // 페어 중복 체크용 64bit 해시
    public static long hash(Long a, Long b) {
        Long[] p = ordered(a, b);
        return (p[0] << 32) | (p[1] & 0xFFFFFFFFL);
    }

    public static String pairKey(Long a, Long b) {
        Long[] p = ordered(a, b);
        return p[0] + "-" + p[1];
    }
}
