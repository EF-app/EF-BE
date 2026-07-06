package com.nokcha.efbe.domain.payment.model;

/**
 * 유저 등급. user_palette.premium_until 이 미래면 PALETTE, 아니면 NORMAL.
 * code_item 의 normal_value / palette_value 선택 기준.
 */
public enum UserTier {
    NORMAL,
    PALETTE
}
