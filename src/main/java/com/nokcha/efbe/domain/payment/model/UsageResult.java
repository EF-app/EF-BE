package com.nokcha.efbe.domain.payment.model;

/**
 * 아이템 1회 사용 결과 — 재원(무료/잉크), 차감 방울, 사용 직후 남은 무료 횟수.
 *  {@code remaining} 은 표시용(집행은 카운터 가드가 담당). 무제한 등급이면 {@code unlimited=true} · {@code remaining=null}.
 */
public record UsageResult(UsageSource source, int inkCost, Integer remaining, boolean unlimited) {

    public boolean isPaid() {
        return source == UsageSource.INK;
    }
}
