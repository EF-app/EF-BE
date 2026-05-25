package com.nokcha.efbe.domain.admin.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 어드민 프로필 반려 요청
@Getter
@NoArgsConstructor
@Schema(description = "프로필 반려 요청 — 사유는 유저에게 안내됨")
public class AdminProfileRejectReqDto {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "반려 사유 (유저에게 노출, 최대 255자)",
            example = "프로필 사진이 규정에 맞지 않습니다",
            maxLength = 255,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
