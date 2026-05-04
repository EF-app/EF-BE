package com.nokcha.efbe.domain.user.dto.request;

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

    @Schema(description = "서비스 이용약관 버전", example = "v1.0")
    private String serviceTermsVersion;

    @AssertTrue(message = "개인정보 처리방침 동의는 필수입니다.")
    @Schema(description = "개인정보 처리방침 동의 여부", example = "true")
    private boolean privacyPolicyAgreed;

    @Schema(description = "개인정보 처리방침 버전", example = "v1.01")
    private String privacyPolicyVersion;

    @AssertTrue(message = "민감정보 수집 및 이용 동의는 필수입니다.")
    @Schema(description = "민감정보 수집 및 이용 동의 여부", example = "true")
    private boolean sensitiveInfoAgreed;

    @Schema(description = "민감정보 수집 및 이용 동의 버전", example = "v1.0")
    private String sensitiveInfoVersion;

    @AssertTrue(message = "개인정보 관련 약관 동의는 필수입니다.")
    @Schema(description = "개인정보 관련 약관 동의 여부", example = "true")
    private boolean personalInformationAgreed;

    @Schema(description = "개인정보 관련 약관 버전", example = "v1.0")
    private String personalInformationVersion;

    @Schema(description = "위치정보 수집 및 이용 동의 여부", example = "true")
    private boolean locationAgreed;

    @Schema(description = "위치정보 수집 및 이용 동의 버전", example = "v1.0")
    private String locationVersion;

    @AssertTrue(message = "본인 연령 확인은 필수입니다.")
    @Schema(description = "성인 여부 확인", example = "true")
    private boolean ageConfirmed;

    @Schema(description = "마케팅 정보 수신 동의 여부", example = "false")
    private boolean marketingAgreed;

    @Schema(description = "마케팅 수신 동의 버전", example = "v1.0")
    private String marketingVersion;

    @Schema(description = "푸시 알림 수신 동의 여부", example = "false")
    private boolean pushAgreed;
}
