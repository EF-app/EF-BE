package com.nokcha.efbe.domain.payment.entity;

// payment_logs.refund_type (v1.7)
public enum RefundType {
    FULL,          // 전액 환불
    PARTIAL,       // 부분 환불
    SYSTEM_ERROR   // 시스템 오류 보상
}
