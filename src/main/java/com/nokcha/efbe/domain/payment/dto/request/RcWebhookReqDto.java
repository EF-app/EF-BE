package com.nokcha.efbe.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RevenueCat webhook 페이로드 — 외부(RevenueCat) 포맷이라 snake_case. 필요한 필드만 매핑.
 * https://www.revenuecat.com/docs/webhooks
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RcWebhookReqDto(@JsonProperty("event") RcEvent event) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RcEvent(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,                         // INITIAL_PURCHASE / RENEWAL / NON_RENEWING_PURCHASE / CANCELLATION / EXPIRATION ...
            @JsonProperty("app_user_id") String appUserId,             // = 우리 userId (Purchases.logIn)
            @JsonProperty("product_id") String productId,              // 스토어 상품 ID
            @JsonProperty("store") String store,                       // APP_STORE / PLAY_STORE
            @JsonProperty("environment") String environment,          // SANDBOX / PRODUCTION
            @JsonProperty("expiration_at_ms") Long expirationAtMs,     // 구독 만료(절대)
            @JsonProperty("transaction_id") String transactionId,
            @JsonProperty("original_transaction_id") String originalTransactionId
    ) {
    }
}
