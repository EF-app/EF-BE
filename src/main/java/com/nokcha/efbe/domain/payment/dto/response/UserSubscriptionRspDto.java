package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.UserSubscription;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 유저 구독 응답 DTO (v1.2 이력형 B — canceledAt / anyFeatureUsed / isActive 노출)
@Getter
@Builder
public class UserSubscriptionRspDto {
    private Long id;
    private Long userId;
    private Integer planId;
    private LocalDateTime startedAt;
    private LocalDateTime endDate;
    private LocalDateTime canceledAt;
    private Boolean autoRenew;
    private Boolean anyFeatureUsed;
    private Boolean isActive;

    public static UserSubscriptionRspDto from(UserSubscription s) {
        return UserSubscriptionRspDto.builder()
                .id(s.getId())
                .userId(s.getUserId())
                .planId(s.getPlanId())
                .startedAt(s.getStartedAt())
                .endDate(s.getEndDate())
                .canceledAt(s.getCanceledAt())
                .autoRenew(Boolean.TRUE.equals(s.getAutoRenew()))
                .anyFeatureUsed(Boolean.TRUE.equals(s.getAnyFeatureUsed()))
                .isActive(Boolean.TRUE.equals(s.getIsActive()))
                .build();
    }
}
