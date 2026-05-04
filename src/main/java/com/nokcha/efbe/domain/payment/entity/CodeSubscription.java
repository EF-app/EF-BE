package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 구독 플랜 마스터 엔티티 (code_subscription)
@Getter
@Entity
@Table(name = "code_subscription",
        uniqueConstraints = {@UniqueConstraint(name = "uk_plan_code", columnNames = "plan_code")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "plan_code", nullable = false, length = 20)
    private String planCode;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays = 30;

    @Column(name = "monthly_free_items", columnDefinition = "JSON")
    private String monthlyFreeItems;

    @Column(name = "benefits", columnDefinition = "JSON")
    private String benefits;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Builder
    private CodeSubscription(String planCode, String name, BigDecimal price, Integer durationDays,
                             String monthlyFreeItems, String benefits, Boolean isActive) {
        this.planCode = planCode;
        this.name = name;
        this.price = price;
        this.durationDays = durationDays == null ? 30 : durationDays;
        this.monthlyFreeItems = monthlyFreeItems;
        this.benefits = benefits;
        this.isActive = isActive == null ? Boolean.TRUE : isActive;
    }
}
