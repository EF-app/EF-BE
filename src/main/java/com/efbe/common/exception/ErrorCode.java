package com.efbe.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 회원가입
    TERMS_AGREEMENT_REQUIRED(400, "필수 약관 동의가 필요합니다."),
    PASSWORD_CONFIRM_MISMATCH(400, "비밀번호 확인이 일치하지 않습니다."),
    PHONE_VERIFICATION_REQUIRED(400, "휴대폰 인증이 필요합니다."),
    FEMALE_VERIFICATION_REQUIRED(400, "여성 인증이 필요합니다."),
    ADULT_VERIFICATION_REQUIRED(400, "성인 인증이 필요합니다."),
    CREDENTIALS_REQUIRED(400, "아이디와 비밀번호 입력이 필요합니다."),
    BASIC_INFO_REQUIRED(400, "닉네임과 지역 입력이 필요합니다."),
    PURPOSE_REQUIRED(400, "가입 목적 선택이 필요합니다."),
    PROFILE_REQUIRED(400, "프로필 정보 입력이 필요합니다."),
    PROFILE_IMAGE_COUNT_EXCEEDED(400, "프로필 이미지는 최대 3장까지 등록할 수 있습니다."),
    INVALID_PROFILE_IMAGE(400, "유효하지 않은 프로필 이미지입니다."),
    PERSONAL_REQUIRED(400, "성향 정보는 1개 이상 선택해야 합니다."),
    IDEAL_PERSONAL_COUNT_INVALID(400, "이상형 중요 포인트는 최대 6개까지 선택할 수 있습니다."),
    INTRODUCTION_REQUIRED(400, "한 줄 소개는 필수입니다."),
    ALCOHOL_REQUIRED(400, "음주 여부는 필수입니다."),
    SMOKING_REQUIRED(400, "흡연 여부는 필수입니다."),
    TATTOO_REQUIRED(400, "타투 여부는 필수입니다."),
    INVALID_ALCOHOL_ID(400, "유효하지 않은 음주 값입니다."),
    INVALID_SMOKING_ID(400, "유효하지 않은 흡연 값입니다."),
    INVALID_TATTOO_ID(400, "유효하지 않은 타투 값입니다."),
    INVALID_REGISTRATION_TOKEN(401, "유효하지 않은 회원가입 토큰입니다."),
    EXPIRED_REGISTRATION_TOKEN(401, "만료된 회원가입 토큰입니다."),
    SIGNUP_SESSION_NOT_FOUND(404, "회원가입 정보를 찾을 수 없습니다."),
    INTEREST_NOT_FOUND(404, "존재하지 않는 관심사입니다."),
    PERSONAL_NOT_FOUND(404, "존재하지 않는 성향 정보입니다."),
    INVALID_IDEAL_PERSONAL_CATEGORY(400, "이상형 선택은 머리, 체형, 키, 성향만 가능합니다."),
    INVALID_LOGIN(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    WITHDRAWN_USER(403, "탈퇴한 회원은 로그인할 수 없습니다."),
    ALREADY_USER(409, "이미 존재하는 유저입니다."),
    ALREADY_PHONE(409, "이미 가입된 휴대폰 번호입니다."),
    ALREADY_NICKNAME(409, "이미 사용 중인 닉네임입니다."),
    NICKNAME_REQUIRED(400, "닉네임 입력이 필요합니다."),
    AREA_REQUIRED(400, "지역 선택이 필요합니다.")

    ;

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
