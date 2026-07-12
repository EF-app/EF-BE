package com.nokcha.efbe.domain.payment.model;

/**
 * 결제 상태 (PG 라이프사이클).
 * PENDING → PAID(지급/구독활성) / FAILED / CANCELED, PAID → REFUNDED
 */
public enum PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    CANCELED,
    REFUNDED
}
