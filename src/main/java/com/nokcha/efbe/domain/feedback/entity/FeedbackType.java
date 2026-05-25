package com.nokcha.efbe.domain.feedback.entity;

import java.util.Set;

import static com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode.*;

public enum FeedbackType {
    BUG(Set.of(UI_BROKEN, FEATURE_NOT_WORK, PERFORMANCE, PAYMENT, NOTIFICATION, CHAT, ETC)),
    FEATURE_REQUEST(Set.of(NEW_FEATURE, UX_DESIGN, PERF_IMPROVE, PAYMENT, NOTIFICATION, CHAT, ETC));

    private final Set<FeedbackCategoryCode> allowedCategories;

    FeedbackType(Set<FeedbackCategoryCode> allowedCategories) {
        this.allowedCategories = allowedCategories;
    }

    public boolean allows(FeedbackCategoryCode category) {
        return allowedCategories.contains(category);
    }
}
