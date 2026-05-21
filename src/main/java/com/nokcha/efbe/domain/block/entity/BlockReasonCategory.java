package com.nokcha.efbe.domain.block.entity;

// 유저 차단 사유 카테고리. block.reason_category ENUM 과 1:1.
public enum BlockReasonCategory {
    UNCOMFORTABLE_BEHAVIOR,  // 불편한 대화나 행동
    PROFANITY_HATE,          // 욕설/혐오
    SEXUAL_CONTENT,          // 음란/성적
    SPAM_PROMOTION,          // 사기/홍보/스팸
    THREAT,                  // 협박/위협
    UNWANTED_CONTACT,        // 이유 없이 연락, 원치 않음
    FAKE_IDENTITY,           // 허위정보/사칭
    OTHER                    // 기타
}
