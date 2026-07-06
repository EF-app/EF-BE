package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.payment.model.ItemResetPeriod;
import com.nokcha.efbe.domain.payment.model.ItemValueType;
import com.nokcha.efbe.domain.payment.model.UserTier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아이템/기능 정책 마스터 — 등급별 한도의 단일 진실(SoT).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "code_item")
public class CodeItem extends BaseEntity {

    @Id
    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 15)
    private ItemValueType valueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_period", nullable = false, length = 10)
    private ItemResetPeriod resetPeriod;

    /** 무료 소진 후 1회 구매 잉크 방울. null = 구매 불가. */
    @Column(name = "ink_cost")
    private Integer inkCost;

    /** 일반 등급 값 (-1 = 무제한). */
    @Column(name = "normal_value", nullable = false)
    private int normalValue;

    /** 팔레트 등급 값 (-1 = 무제한). */
    @Column(name = "palette_value", nullable = false)
    private int paletteValue;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private CodeItem(String itemCode, String name, ItemValueType valueType, ItemResetPeriod resetPeriod,
                     Integer inkCost, int normalValue, int paletteValue, boolean isActive, int sortOrder) {
        this.itemCode = itemCode;
        this.name = name;
        this.valueType = valueType;
        this.resetPeriod = resetPeriod;
        this.inkCost = inkCost;
        this.normalValue = normalValue;
        this.paletteValue = paletteValue;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
    }

    /** 등급에 해당하는 한도값 (-1 = 무제한). */
    public int resolveValue(UserTier tier) {
        return tier == UserTier.PALETTE ? paletteValue : normalValue;
    }

    /** 무료 소진 후 잉크로 구매 가능한 아이템인지. */
    public boolean isPurchasable() {
        return inkCost != null;
    }

    /** 관리자 한도/단가 조정 진입점. */
    public void applyUpdate(Integer inkCost, int normalValue, int paletteValue, boolean isActive) {
        this.inkCost = inkCost;
        this.normalValue = normalValue;
        this.paletteValue = paletteValue;
        this.isActive = isActive;
    }
}
