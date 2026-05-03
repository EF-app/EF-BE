package com.nokcha.efbe.domain.user.entity;

public enum SignUpStep {
    TERMS_AGREED,
    PHONE_VERIFIED,
    EMAIL_VERIFIED,
    CREDENTIALS_COMPLETED,
    NICKNAME_COMPLETED,
    AREA_COMPLETED,
    PURPOSE_SELECTED,
    INTEREST_COMPLETED,
    LIFESTYLE_COMPLETED,
    ABOUT_ME_COMPLETED,
    IDEAL_COMPLETED,
    PROFILE_COMPLETED,
    SIGNUP_COMPLETED;

    // 현재 단계가 비교 대상 단계 이상인지 확인
    public boolean isAtLeast(SignUpStep targetStep) {
        return this.ordinal() >= targetStep.ordinal();
    }
}
