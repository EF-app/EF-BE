package com.nokcha.efbe.domain.feedback.entity;

import io.swagger.v3.oas.annotations.media.Schema;

// 피드백 처리 상태 — DB feedback.status 컬럼
@Schema(description = "피드백 처리 상태 — RECEIVED(접수됨), IN_REVIEW(검토 중), IN_PROGRESS(처리 중), RESOLVED(해결됨), DEFERRED(보류), CLOSED(종료)")
public enum FeedbackStatus {
    RECEIVED,
    IN_REVIEW,
    IN_PROGRESS,
    RESOLVED,
    DEFERRED,
    CLOSED
}
