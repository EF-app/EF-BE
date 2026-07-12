package com.nokcha.efbe.domain.payment.model;

/**
 * 결제 환경. RevenueCat webhook 의 environment 매핑 — 샌드박스 테스트 결제 구분.
 */
public enum PaymentEnvironment {
    SANDBOX,
    PRODUCTION
}
