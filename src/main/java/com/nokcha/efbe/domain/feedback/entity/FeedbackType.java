package com.nokcha.efbe.domain.feedback.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

import static com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode.*;

// 피드백 유형 — DB feedback.feedback_type 컬럼
@Schema(description = "피드백 유형 — BUG=버그신고, FEATURE_REQUEST=기능요청")
public enum FeedbackType {
    BUG(Set.of(UI_BROKEN, FEATURE_NOT_WORK, PERFORMANCE, PAYMENT, NOTIFICATION, CHAT)),
    FEATURE_REQUEST(Set.of(NEW_FEATURE, UX_DESIGN, PERF_IMPROVE, PAYMENT, NOTIFICATION, CHAT));

    private final Set<FeedbackCategoryCode> allowedCategories;

    FeedbackType(Set<FeedbackCategoryCode> allowedCategories) {
        this.allowedCategories = allowedCategories;
    }

    public boolean allows(FeedbackCategoryCode category) {
        return allowedCategories.contains(category);
    }
}
