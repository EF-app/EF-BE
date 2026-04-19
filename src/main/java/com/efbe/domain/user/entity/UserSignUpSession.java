package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
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
    private boolean ageConfirmed;

    @Column(nullable = false)
    private boolean femaleConfirmed;

    @Column(nullable = false)
    private boolean marketingAgreed;

    @Column(length = 20)
    private String phone;

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

    @Builder
    public UserSignUpSession(
            boolean serviceTermsAgreed,
            boolean privacyPolicyAgreed,
            boolean ageConfirmed,
            boolean femaleConfirmed,
            boolean marketingAgreed,
            String phone,
            String loginId,
            String password,
            String nickname,
            Long areaId,
            Purpose purpose,
            SignUpStep signUpStep,
            LocalDateTime expiredAt,
            boolean completed,
            LocalDateTime phoneVerifiedAt
    ) {
        this.serviceTermsAgreed = serviceTermsAgreed;
        this.privacyPolicyAgreed = privacyPolicyAgreed;
        this.ageConfirmed = ageConfirmed;
        this.femaleConfirmed = femaleConfirmed;
        this.marketingAgreed = marketingAgreed;
        this.phone = phone;
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.areaId = areaId;
        this.purpose = purpose;
        this.signUpStep = signUpStep;
        this.expiredAt = expiredAt;
        this.completed = completed;
        this.phoneVerifiedAt = phoneVerifiedAt;
    }

    // 필수 약관 동의 여부 확인
    public boolean hasRequiredTermsAgreed() {
        return serviceTermsAgreed && privacyPolicyAgreed;
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

    // 아이디, 비밀번호 저장
    public void updateCredentials(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
        this.signUpStep = SignUpStep.CREDENTIALS_COMPLETED;
    }

    // 닉네임 및 지역 저장
    public void updateBasicInfo(String nickname, Long areaId) {
        this.nickname = nickname;
        this.areaId = areaId;
        this.signUpStep = SignUpStep.BASIC_INFO_COMPLETED;
    }


    // 회원가입 목적 저장
    public void updatePurpose(Purpose purpose) {
        this.purpose = purpose;
        this.signUpStep = SignUpStep.PURPOSE_SELECTED;
    }

    // 프로핈 설정 완료 상태로 변경
    public void updateProfileStep() {
        this.signUpStep = SignUpStep.PROFILE_COMPLETED;
    }

    // 회원가입 완료 상태로 변경
    public void completeSignUp() {
        this.completed = true;
        this.signUpStep = SignUpStep.SIGNUP_COMPLETED;
    }
}
