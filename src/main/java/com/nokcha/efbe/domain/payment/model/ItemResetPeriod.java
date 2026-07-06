package com.nokcha.efbe.domain.payment.model;

/**
 * COUNT 아이템의 무료 한도 리셋 주기 → period_key 포맷 결정.
 * DAILY=yyyy-MM-dd · WEEKLY=yyyy-'W'ww · MONTHLY=yyyy-MM · NONE=리셋 없음
 */
public enum ItemResetPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    NONE
}
