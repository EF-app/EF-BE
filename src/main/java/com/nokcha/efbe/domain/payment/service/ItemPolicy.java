package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.domain.payment.entity.CodeItem;
import com.nokcha.efbe.domain.payment.model.UserTier;

/**
 * 특정 유저·아이템에 대해 해석된 정책 캐리어 — 마스터 + 등급 + 한도값.
 * {@link PlanLimitResolver} 가 조립해 전달하는 서비스 내부 캐리어(엔티티 보유 → service 배치).
 */
public class ItemPolicy {

    private final CodeItem item;
    private final UserTier tier;
    private final int value;

    public ItemPolicy(CodeItem item, UserTier tier, int value) {
        this.item = item;
        this.tier = tier;
        this.value = value;
    }

    public CodeItem item() {
        return item;
    }

    public UserTier tier() {
        return tier;
    }

    /** 한도/수치값 (-1 = 무제한). */
    public int value() {
        return value;
    }

    public boolean isUnlimited() {
        return value < 0;
    }

    public boolean isPurchasable() {
        return item.isPurchasable();
    }

    /** 구매 단가(방울). 구매 불가면 null. */
    public Integer inkCost() {
        return item.getInkCost();
    }

    public String itemCode() {
        return item.getItemCode();
    }
}
