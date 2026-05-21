package com.nokcha.efbe.domain.admin.feedback.dto.response;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 어드민 피드백 응답 — 목록/상세 공용
@Getter
@Builder
public class AdminFeedbackRspDto {

    private Long id;
    private Long reporterId;
    private String reporterNickname;
    private String reporterLoginId;
    private FeedbackType feedbackType;
    private FeedbackCategoryCode categoryCode;
    private String title;
    private String content;
    private List<String> screenshotUrls;
    private String appVersion;
    private String deviceInfo;
    private String networkType;
    private FeedbackStatus status;
    private String adminReply;
    private LocalDateTime adminReplyAt;
    private Long adminHandlerId;
    private String adminHandlerName;
    private String adminInternalMemo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static AdminFeedbackRspDto of(Feedback f, List<FeedbackImage> images) {
        User reporter = f.getReporter();
        AdminAccount handler = f.getAdminHandler();
        return AdminFeedbackRspDto.builder()
                .id(f.getId())
                .reporterId(reporter == null ? null : reporter.getId())
                .reporterNickname(reporter == null ? null : reporter.getNickname())
                .reporterLoginId(reporter == null ? null : reporter.getLoginId())
                .feedbackType(f.getFeedbackType())
                .categoryCode(f.getCategoryCode())
                .title(f.getTitle())
                .content(f.getContent())
                .screenshotUrls(images == null ? List.of()
                        : images.stream().map(FeedbackImage::getUrl).toList())
                .appVersion(f.getAppVersion())
                .deviceInfo(f.getDeviceInfo())
                .networkType(f.getNetworkType())
                .status(f.getStatus())
                .adminReply(f.getAdminReply())
                .adminReplyAt(f.getAdminReplyAt())
                .adminHandlerId(handler == null ? null : handler.getId())
                .adminHandlerName(handler == null ? null : handler.getName())
                .adminInternalMemo(f.getAdminInternalMemo())
                .createTime(f.getCreateTime())
                .updateTime(f.getUpdateTime())
                .build();
    }
}
