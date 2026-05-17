package com.nokcha.efbe.domain.report.entity;

// 신고 대상 도메인
public enum ReportTargetType {
    POST_IT,
    BAL_COMMENT,
    PROFILE,    // PROFILE 의 경우 target_id 는 신고 대상 user.id
    CHAT,
    CHAT_IMAGE
}
