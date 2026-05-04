package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.CodeSubscription;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

// 구독 플랜 응답 DTO
@Getter
@Builder
public class SubscriptionPlanRspDto {
    private Integer id;
    private String planCode;
    private String name;
    private BigDecimal price;
    private Integer durationDays;
    private String monthlyFreeItems;
    private String benefits;
    private boolean active;

    public static SubscriptionPlanRspDto from(CodeSubscription p) {
        return SubscriptionPlanRspDto.builder()
                .id(p.getId())
                .planCode(p.getPlanCode())
                .name(p.getName())
                .price(p.getPrice())
                .durationDays(p.getDurationDays())
                .monthlyFreeItems(p.getMonthlyFreeItems())
                .benefits(p.getBenefits())
                .active(Boolean.TRUE.equals(p.getIsActive()))
                .build();
    }
}
