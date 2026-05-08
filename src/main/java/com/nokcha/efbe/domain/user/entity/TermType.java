package com.nokcha.efbe.domain.user.entity;

public enum TermType {
    TERMS_AGREE,    // 이용약관
    PRIVACY_COLLECTION_AGREE,  // 개인정보 수집 및 이용
    SENSITIVE_AGREE,    // 민감정보
    NO_DISCLOSURE_AGREE, // 타인 정보 외부 유출 금지 동의
    MARKETING_AGREE,    // 마케팅
    PUSH_AGREE,     // 푸시 알림
    LOCATION_AGREE, // 위치 정보
    PRIVACY_POLICY  // 개인정보 처리방침 (정보 제공용, 동의 토글 X)
}