package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 신청 승인/반려 요청 DTO (관리자용)
@Getter
@NoArgsConstructor
@Schema(description = "신청 승인/반려 요청 (관리자용)")
public class BalApplyDecisionReqDto {

    @Schema(description = "결정 상태 (APPROVED / REJECTED 등)", example = "APPROVED")
    @NotNull
    private BalApplyStatus status;

    @Schema(description = "관리자 메모", maxLength = 255)
    @Size(max = 255)
    private String adminMemo;
}
