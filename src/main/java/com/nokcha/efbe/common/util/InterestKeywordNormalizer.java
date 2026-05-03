package com.nokcha.efbe.common.util;

import java.util.Locale;

public final class InterestKeywordNormalizer {

    private InterestKeywordNormalizer() {
    }

    // 관심사 비교용 키워드 정규화 (공백 제거 및 소문자화)
    public static String normalize(String keyword) {
        if (keyword == null) return null;

        String normalized = keyword.trim().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) return null;

        return normalized.toLowerCase(Locale.ROOT);
    }
}
