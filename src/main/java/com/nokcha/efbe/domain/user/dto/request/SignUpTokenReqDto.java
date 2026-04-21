package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 완료 DTO")
public class SignUpTokenReqDto {
    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "발급된 회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;
}
