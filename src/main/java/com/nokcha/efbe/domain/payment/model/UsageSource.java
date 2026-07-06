package com.nokcha.efbe.domain.payment.model;

/**
 * 아이템 사용 재원.
 * FREE — 등급 무료 한도 소진  ·  INK — 무료 소진 후 잉크 차감 (ink_history_id 연결)
 */
public enum UsageSource {
    FREE,
    INK
}
