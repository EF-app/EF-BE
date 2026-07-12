package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "탈퇴 화면 안내 데이터 — 유예 일수/파기 예정/잉크·구독 상태.")
public class WithdrawalPreviewRspDto {

    @Schema(description = "탈퇴 유예 일수(이 기간 내 철회 가능)", example = "30")
    private int graceDays;

    @Schema(description = "지금 탈퇴를 신청할 경우 예정되는 파기 시각(미리보기). now + 유예일.",
            example = "2026-08-10T14:30:00")
    private LocalDateTime scheduledDestroyAt;

    @Schema(description = "잔여 잉크. payment 도메인 미연동 상태이므로 현재는 null.",
            example = "0", nullable = true)
    private Integer inkBalance;

    @Schema(description = "활성 정기 구독 보유 여부. payment 도메인 미연동 상태이므로 현재는 null.",
            example = "false", nullable = true)
    private Boolean hasActiveSubscription;

    @Schema(description = "구독 만료 예정일. payment 도메인 미연동 상태이므로 현재는 null.",
            example = "2026-08-01T00:00:00", nullable = true)
    private LocalDateTime subscriptionExpiresAt;

    @Schema(description = "잉크/구독 정보 연동 여부. false 면 inkBalance/hasActiveSubscription 은 아직 신뢰할 수 없어 화면에서 숨김 처리 권장.",
            example = "false")
    private boolean paymentInfoAvailable;
}
