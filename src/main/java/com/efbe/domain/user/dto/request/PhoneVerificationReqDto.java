package com.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
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
@Schema(description = "회원가입 휴대폰 인증 요청")
public class PhoneVerificationReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "약관 동의 완료 후 발급된 회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "휴대폰 번호 형식이 올바르지 않습니다.")
    @Schema(description = "휴대폰 번호", example = "01012345678")
    private String phone;

    @AssertTrue(message = "성인 인증은 필수입니다.")
    @Schema(description = "성인 인증 여부", example = "true")
    private boolean adultVerified;

    @AssertTrue(message = "여성 인증은 필수입니다.")
    @Schema(description = "여성 인증 여부", example = "true")
    private boolean femaleVerified;
}
