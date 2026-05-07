package com.nokcha.efbe.domain.feedback.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 피드백(버그신고/기능요청) 엔티티 — feedback 테이블
@Getter
@Entity
@Table(name = "feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 신고자 — users(id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feedback_reporter"))
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 30)
    private FeedbackType feedbackType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 30)
    private FeedbackCategoryCode categoryCode;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // R2 업로드된 스크린샷 URL 배열을 JSON 문자열로 저장
    @Column(name = "screenshot_urls", columnDefinition = "JSON")
    private String screenshotUrls;

    @Column(name = "app_version", length = 30)
    private String appVersion;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Column(name = "network_type", length = 20)
    private String networkType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeedbackStatus status;

    @Column(name = "admin_reply", columnDefinition = "TEXT")
    private String adminReply;

    @Column(name = "admin_reply_at")
    private LocalDateTime adminReplyAt;

    // 담당 관리자 — admin_account(id) (등록 시점엔 null)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_handler_id",
            foreignKey = @ForeignKey(name = "fk_feedback_handler"))
    private Admin adminHandler;

    @Column(name = "admin_internal_memo", length = 1000)
    private String adminInternalMemo;

    @Builder
    private Feedback(User reporter, FeedbackType feedbackType, FeedbackCategoryCode categoryCode,
                     String title, String content, String screenshotUrls,
                     String appVersion, String deviceInfo, String networkType,
                     FeedbackStatus status) {
        this.reporter = reporter;
        this.feedbackType = feedbackType;
        this.categoryCode = categoryCode;
        this.title = title;
        this.content = content;
        this.screenshotUrls = screenshotUrls;
        this.appVersion = appVersion;
        this.deviceInfo = deviceInfo;
        this.networkType = networkType;
        this.status = status == null ? FeedbackStatus.RECEIVED : status;
    }
}
