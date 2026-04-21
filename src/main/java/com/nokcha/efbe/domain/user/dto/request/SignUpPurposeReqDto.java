package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.profile.entity.Purpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 목적 선택 요청")
public class SignUpPurposeReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @NotNull(message = "가입 목적은 필수입니다.")
    @Schema(description = "가입 목적", example = "LOVE")
    private Purpose purpose;
}
