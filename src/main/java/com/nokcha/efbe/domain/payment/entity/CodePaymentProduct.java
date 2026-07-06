package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.payment.model.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 결제 상품 마스터 — 잉크 팩(INK) / 구독 플랜(PALETTE).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "code_payment_product",
        uniqueConstraints = @UniqueConstraint(name = "uk_product_code", columnNames = "product_code"))
public class CodePaymentProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 10)
    private ProductType productType;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 판매가 (원). */
    @Column(name = "price", nullable = false)
    private int price;

    /** [INK] 지급 방울. PALETTE 는 null. */
    @Column(name = "ink_amount")
    private Integer inkAmount;

    /** [PALETTE] 지급 일수. INK 는 null. */
    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private CodePaymentProduct(String productCode, ProductType productType, String name, int price,
                               Integer inkAmount, Integer durationDays, boolean isActive, int sortOrder) {
        this.productCode = productCode;
        this.productType = productType;
        this.name = name;
        this.price = price;
        this.inkAmount = inkAmount;
        this.durationDays = durationDays;
        this.isActive = isActive;
        this.sortOrder = sortOrder;
    }
}
