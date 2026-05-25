package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보안코드 초기화(리셋) 요청 — 기존 보안코드를 잊은 경우 비밀번호 재인증으로 새 값 설정")
public class ScodeResetReqDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호 (재인증용)", example = "Ef123456!")
    private String password;

    @NotBlank(message = "새 보안코드는 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "새 보안코드", example = "5678")
    private String newScode;

    @NotBlank(message = "새 보안코드 확인은 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "새 보안코드 확인", example = "5678")
    private String newScodeConfirm;
}
