package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.UserPalette;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "팔레트 구독 상태")
public class SubscriptionRspDto {

    @Schema(description = "현재 프리미엄 여부 (premium_until > now)", example = "true")
    private boolean premium;

    @Schema(description = "구독 만료 시각. 무료면 null.", example = "2026-08-07T00:00:00", nullable = true)
    private LocalDateTime premiumUntil;

    @Schema(description = "자동갱신 ON 여부", example = "true")
    private boolean autoRenew;

    @Schema(description = "해지 요청 시각. 미해지면 null. (auto_renew=false & premium=true 면 '해지했지만 만료일까지 유지')",
            example = "2026-07-20T09:00:00", nullable = true)
    private LocalDateTime canceledAt;

    public static SubscriptionRspDto from(UserPalette p, LocalDateTime now) {
        return SubscriptionRspDto.builder()
                .premium(p.isPremium(now))
                .premiumUntil(p.getPremiumUntil())
                .autoRenew(p.isAutoRenew())
                .canceledAt(p.getCanceledAt())
                .build();
    }

    /** 구독 이력이 없는 유저 — 무료. */
    public static SubscriptionRspDto free() {
        return SubscriptionRspDto.builder().premium(false).autoRenew(false).build();
    }
}
