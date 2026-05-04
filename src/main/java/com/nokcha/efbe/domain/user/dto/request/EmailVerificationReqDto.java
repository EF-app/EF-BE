package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 이메일 인증 요청")
public class EmailVerificationReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "인증할 이메일", example = "user@example.com")
    private String email;

    @AssertTrue(message = "이메일 인증 완료 여부는 true 여야 합니다.")
    @Schema(description = "이메일 인증 완료 여부", example = "true")
    private boolean emailVerified;
}
