package com.nokcha.efbe.domain.feedback.entity;

import io.swagger.v3.oas.annotations.media.Schema;

// 피드백 카테고리 — DB feedback.category_code 컬럼
// BUG: UI_BROKEN/FEATURE_NOT_WORK/PERFORMANCE/PAYMENT/NOTIFICATION/CHAT/ETC
// FEATURE_REQUEST: NEW_FEATURE/UX_DESIGN/PERF_IMPROVE/PAYMENT/NOTIFICATION/CHAT/ETC
@Schema(description = """
        피드백 카테고리.
        BUG 유형: UI_BROKEN(화면이 깨져요), FEATURE_NOT_WORK(기능이 동작하지 않아요), PERFORMANCE(느리거나 버벅거려요), PAYMENT(결제 관련), NOTIFICATION(알림 문제), CHAT(채팅 문제), ETC(기타).
        FEATURE_REQUEST 유형: NEW_FEATURE(새로운 기능 제안), UX_DESIGN(디자인 개선), PERF_IMPROVE(성능 개선), PAYMENT(결제 관련), NOTIFICATION(알림 개선), CHAT(채팅 개선), ETC(기타).""")
public enum FeedbackCategoryCode {
    // BUG 전용
    UI_BROKEN,
    FEATURE_NOT_WORK,
    PERFORMANCE,

    // FEATURE_REQUEST 전용
    NEW_FEATURE,
    UX_DESIGN,
    PERF_IMPROVE,

    // BUG/FEATURE_REQUEST 공용
    PAYMENT,
    NOTIFICATION,
    CHAT,
    ETC
}
