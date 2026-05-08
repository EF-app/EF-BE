package com.nokcha.efbe.domain.faq.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

// FAQ 카테고리 — DB code_faq.category 컬럼 (대문자 ENUM).
// API 응답/요청에서는 FE 와 맞추기 위해 소문자 형태(apiKey)를 사용한다.
@Schema(description = "FAQ 카테고리 — ACCOUNT(계정), MATCHING(매칭), MESSAGE(메시지), PAYMENT(결제), REPORT(신고/차단), ETC(기타). API 직렬화 시 소문자 형태(account, matching, ...)로 노출.")
public enum FaqCategory {
    ACCOUNT,
    MATCHING,
    MESSAGE,
    PAYMENT,
    REPORT,
    ETC;

    public String apiKey() {
        return name().toLowerCase();
    }

    // FE 가 보낸 소문자 키를 enum 으로 매핑. 매칭 안 되면 null.
    public static FaqCategory fromApiKey(String key) {
        if (key == null || key.isBlank()) return null;
        return Arrays.stream(values())
                .filter(c -> c.apiKey().equalsIgnoreCase(key))
                .findFirst()
                .orElse(null);
    }
}
