package com.nokcha.efbe.domain.payment.model;

import com.nokcha.efbe.domain.payment.entity.CodeItem;

/**
 * 특정 유저·아이템에 대해 해석된 정책 캐리어 — 마스터 + 등급 + 한도값
 */
public record ItemPolicy(CodeItem item, UserTier tier, int value) {

    /** -1 = 무제한. */
    public boolean isUnlimited() {
        return value < 0;
    }

    /** 무료 소진 후 잉크 구매 가능 여부. */
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
