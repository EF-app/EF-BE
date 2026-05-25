package com.nokcha.efbe.domain.feedback.entity;

public enum FeedbackStatus {
    RECEIVED,   // 접수됨
    IN_REVIEW,  // 검토 중
    IN_PROGRESS,    // 처리 중
    RESOLVED,   // 해결됨
    DEFERRED,   // 보류
    CLOSED  // 종료
}
