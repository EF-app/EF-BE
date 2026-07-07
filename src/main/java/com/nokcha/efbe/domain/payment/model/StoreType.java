package com.nokcha.efbe.domain.payment.model;

/**
 * 인앱결제 스토어. RevenueCat webhook 의 store 필드 매핑.
 * APPLE — App Store  ·  GOOGLE — Play Store
 */
public enum StoreType {
    APPLE,
    GOOGLE
}
