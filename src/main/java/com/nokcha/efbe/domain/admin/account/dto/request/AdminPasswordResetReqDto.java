package com.nokcha.efbe.domain.admin.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 다른 관리자 비밀번호 강제 변경 (PATCH /v1/admin/account/{id}/password)
@Getter
@NoArgsConstructor
@Schema(description = "관리자 비밀번호 강제 변경 요청 — 현재 비밀번호 확인 없이 즉시 교체 (관리자 권한)")
public class AdminPasswordResetReqDto {

    @NotBlank
    @Size(min = 8, max = 64)
    @Schema(description = "새 비밀번호 (평문, 8~64자) — BE 에서 bcrypt 해시 후 저장",
            example = "TempReset!234",
            minLength = 8,
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
