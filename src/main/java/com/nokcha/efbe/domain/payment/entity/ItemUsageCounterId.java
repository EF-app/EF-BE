package com.nokcha.efbe.domain.payment.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 복합 PK — (user_id, item_code, period_key).
 * period_key 가 키에 포함돼 주기가 바뀌면 새 행 → 리셋 배치 없이 자동 초기화.
 */
public class ItemUsageCounterId implements Serializable {

    private Long userId;
    private String itemCode;
    private String periodKey;

    protected ItemUsageCounterId() {
    }

    public ItemUsageCounterId(Long userId, String itemCode, String periodKey) {
        this.userId = userId;
        this.itemCode = itemCode;
        this.periodKey = periodKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemUsageCounterId that)) return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(itemCode, that.itemCode)
                && Objects.equals(periodKey, that.periodKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, itemCode, periodKey);
    }
}
