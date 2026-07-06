package com.nokcha.efbe.domain.payment.model;

/**
 * 결제 상품 유형.
 * INK     — 소모성 잉크 (ink_amount 방울 지급)
 * PALETTE — 기간제 구독 (duration_days 일 지급)
 */
public enum ProductType {
    INK,
    PALETTE
}
