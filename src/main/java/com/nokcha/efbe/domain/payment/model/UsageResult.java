package com.nokcha.efbe.domain.payment.model;

/**
 * 아이템 1회 사용 결과 — 재원(무료/잉크)과 차감 방울.
 */
public record UsageResult(UsageSource source, int inkCost) {

    public boolean isPaid() {
        return source == UsageSource.INK;
    }
}
