package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_signup_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSignUpSession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean serviceTermsAgreed;

    @Column(nullable = false)
    private boolean privacyPolicyAgreed;

    @Column(nullable = false)
    private boolean sensitiveInfoAgreed;

    @Column(nullable = false)
    private boolean personalInformationAgreed;

    @Column(nullable = false)
    private boolean locationAgreed;

    @Column(nullable = false)
    private boolean ageConfirmed;

    @Column(nullable = false)
    private boolean femaleConfirmed;

    @Column(nullable = false)
    private boolean marketingAgreed;

    @Column(nullable = false)
    private boolean pushAgreed;

    @Column(length = 20)
    private String serviceTermsVersion;

    @Column(length = 20)
    private String privacyPolicyVersion;

    @Column(length = 20)
    private String sensitiveInfoVersion;

    @Column(length = 20)
    private String personalInformationVersion;

    @Column(length = 20)
    private String locationVersion;

    @Column(length = 20)
    private String marketingVersion;

    @Column
    private LocalDateTime serviceTermsAgreedAt;

    @Column
    private LocalDateTime privacyPolicyAgreedAt;

    @Column
    private LocalDateTime sensitiveInfoAgreedAt;

    @Column
    private LocalDateTime personalInformationAgreedAt;

    @Column
    private LocalDateTime locationAgreedAt;

    @Column
    private LocalDateTime marketingAgreedAt;

    @Column
    private LocalDateTime pushAgreedAt;

    @Column(length = 45)
    private String lastConsentIp;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 50)
    private String loginId;

    @Column(length = 255)
    private String password;

    @Column(length = 30)
    private String nickname;

    @Column
    private Long areaId;

    @Enumerated(EnumType.STRING)
    private Purpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SignUpStep signUpStep;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private boolean completed;

    @Column
    private LocalDateTime phoneVerifiedAt;

    @Column
    private LocalDateTime emailVerifiedAt;

    @Builder
    public UserSignUpSession(boolean serviceTermsAgreed, boolean privacyPolicyAgreed, boolean sensitiveInfoAgreed, boolean personalInformationAgreed, boolean locationAgreed, boolean ageConfirmed, boolean femaleConfirmed, boolean marketingAgreed, boolean pushAgreed, String serviceTermsVersion, String privacyPolicyVersion, String sensitiveInfoVersion, String personalInformationVersion, String locationVersion, String marketingVersion, LocalDateTime serviceTermsAgreedAt, LocalDateTime privacyPolicyAgreedAt, LocalDateTime sensitiveInfoAgreedAt, LocalDateTime personalInformationAgreedAt, LocalDateTime locationAgreedAt, LocalDateTime marketingAgreedAt, LocalDateTime pushAgreedAt, String lastConsentIp, String phone, String email, String loginId, String password, String nickname, Long areaId, Purpose purpose, SignUpStep signUpStep, LocalDateTime expiredAt, boolean completed, LocalDateTime phoneVerifiedAt, LocalDateTime emailVerifiedAt) {
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.sensitiveInfoAgreed = sensitiveInfoAgreed;
        this.personalInformationAgreed = personalInformationAgreed;
        this.locationAgreed = locationAgreed;
        this.ageConfirmed = ageConfirmed;
        this.femaleConfirmed = femaleConfirmed;
        this.marketingAgreed = marketingAgreed;
        this.pushAgreed = pushAgreed;
        this.serviceTermsVersion = serviceTermsVersion;
        this.privacyPolicyVersion = privacyPolicyVersion;
        this.sensitiveInfoVersion = sensitiveInfoVersion;
        this.personalInformationVersion = personalInformationVersion;
        this.locationVersion = locationVersion;
        this.marketingVersion = marketingVersion;
        this.serviceTermsAgreedAt = serviceTermsAgreedAt;
        this.privacyPolicyAgreedAt = privacyPolicyAgreedAt;
        this.sensitiveInfoAgreedAt = sensitiveInfoAgreedAt;
        this.personalInformationAgreedAt = personalInformationAgreedAt;
        this.locationAgreedAt = locationAgreedAt;
        this.marketingAgreedAt = marketingAgreedAt;
        this.pushAgreedAt = pushAgreedAt;
        this.lastConsentIp = lastConsentIp;
        this.phone = phone;
        this.email = email;
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.areaId = areaId;
        this.purpose = purpose;
        this.signUpStep = signUpStep;
        this.expiredAt = expiredAt;
        this.completed = completed;
        this.phoneVerifiedAt = phoneVerifiedAt;
        this.emailVerifiedAt = emailVerifiedAt;
    }

    // 필수 약관 동의 여부 확인
    public boolean hasRequiredTermsAgreed() {
        return serviceTermsAgreed && privacyPolicyAgreed && sensitiveInfoAgreed && personalInformationAgreed;
    }

    // 휴대폰 인증 완료 여부 확인
    public boolean isPhoneVerified() {
        return phone != null && ageConfirmed && femaleConfirmed && phoneVerifiedAt != null;
    }

    // 회원가입 세션 만료 여부 확인
    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    // 휴대폰 인증 정보 저장
    public void verifyPhone(String phone, boolean ageConfirmed, boolean femaleConfirmed, LocalDateTime phoneVerifiedAt) {
        this.phone = phone;
        this.ageConfirmed = ageConfirmed;
        this.femaleConfirmed = femaleConfirmed;
        this.phoneVerifiedAt = phoneVerifiedAt;
        this.signUpStep = SignUpStep.PHONE_VERIFIED;
    }

    // 이메일 인증 정보 저장
    public void verifyEmail(String email, LocalDateTime emailVerifiedAt) {
        this.email = email;
        this.emailVerifiedAt = emailVerifiedAt;
        this.signUpStep = SignUpStep.EMAIL_VERIFIED;
    }

    // 아이디, 비밀번호 저장
    public void updateCredentials(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
        this.signUpStep = SignUpStep.CREDENTIALS_COMPLETED;
    }

    // 닉네임 저장
    public void updateNickname(String nickname) {
        this.nickname = nickname;
        this.signUpStep = SignUpStep.NICKNAME_COMPLETED;
    }

    // 지역 저장
    public void updateArea(Long areaId) {
        this.areaId = areaId;
        this.signUpStep = SignUpStep.AREA_COMPLETED;
    }

    // 회원가입 목적 저장
    public void updatePurpose(Purpose purpose) {
        this.purpose = purpose;
        updateProgressStep(SignUpStep.PURPOSE_SELECTED);
    }

    // 관심사 설정 완료 상태로 변경
    public void updateInterestStep() {
        updateProgressStep(SignUpStep.INTEREST_COMPLETED);
    }

    // 생활 습관 설정 완료 상태로 변경
    public void updateLifestyleStep() {
        updateProgressStep(SignUpStep.LIFESTYLE_COMPLETED);
    }

    // 나에 대해서 설정 완료 상태로 변경
    public void updateAboutMeStep() {
        updateProgressStep(SignUpStep.ABOUT_ME_COMPLETED);
    }

    // 이상형 설정 완료 상태로 변경
    public void updateIdealStep() {
        updateProgressStep(SignUpStep.IDEAL_COMPLETED);
    }

    // 프로필 사진 및 소개 설정 완료 상태로 변경
    public void updateProfileIntroStep() {
        updateProgressStep(SignUpStep.PROFILE_COMPLETED);
    }

    // 회원가입 완료 상태로 변경
    public void completeSignUp() {
        this.completed = true;
        this.signUpStep = SignUpStep.SIGNUP_COMPLETED;
    }

    // 뒷단계가 이미 저장되어 있으면 진행 단계 유지
    private void updateProgressStep(SignUpStep targetStep) {
        if (signUpStep == null || !signUpStep.isAtLeast(targetStep)) {
            this.signUpStep = targetStep;
        }
    }
}
