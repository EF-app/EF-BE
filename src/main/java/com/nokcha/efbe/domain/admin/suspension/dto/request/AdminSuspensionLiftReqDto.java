package com.nokcha.efbe.domain.admin.suspension.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 제재 수동 해제 요청")
public class AdminSuspensionLiftReqDto {

    @NotBlank
    @Size(max = 500)
    @Schema(description = "해제 사유 (이의 신청 수용 등)", example = "이의 신청 수용 — 신고 내용 확인 후 무혐의 판정")
    private String liftedReason;
}
