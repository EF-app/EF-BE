package com.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 약관 동의 요청")
public class TermsAgreementReqDto {

    @AssertTrue(message = "서비스 이용약관 동의는 필수입니다.")
    @Schema(description = "서비스 이용약관 동의 여부", example = "true")
    private boolean serviceTermsAgreed;

    @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
    @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
    private boolean privacyPolicyAgreed;

    @AssertTrue(message = "본인 연령 확인은 필수입니다.")
    @Schema(description = "성인 여부 확인", example = "true")
    private boolean ageConfirmed;

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    private boolean marketingAgreed;
}
