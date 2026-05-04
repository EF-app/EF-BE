package com.nokcha.efbe.domain.user.entity;

public enum TermType {
    TERMS_AGREE,    // 이용양관
    PRIVACY_AGREE,  // 개인정보 처리방침
    SENSITIVE_AGREE,    // 민감정보
    PERSONAL_INFORMATION_AGREE, // 개인정보 유출
    MARKETING_AGREE,    // 마케팅
    PUSH_AGREE,     // 푸시 알림
    LOCATION_AGREE  // 위치 정보
}