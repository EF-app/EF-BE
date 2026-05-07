package com.nokcha.efbe.domain.feedback.dto.response;

import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 피드백 등록 응답
@Getter
@Builder
@Schema(description = "피드백 등록 응답")
public class FeedbackRspDto {

    @Schema(description = "피드백 PK", example = "1")
    private Long id;

    @Schema(description = "신고자 user_id", example = "10")
    private Long reporterId;

    @Schema(description = "피드백 유형", example = "BUG")
    private FeedbackType feedbackType;

    @Schema(description = "피드백 카테고리", example = "UI_BROKEN")
    private FeedbackCategoryCode categoryCode;

    @Schema(description = "요약 제목", example = "프로필 이미지가 깨져서 보여요")
    private String title;

    @Schema(description = "상세 내용")
    private String content;

    @Schema(description = "R2 스크린샷 URL 배열 (없으면 빈 리스트)")
    private List<String> screenshotUrls;

    @Schema(description = "앱 버전")
    private String appVersion;

    @Schema(description = "디바이스 정보")
    private String deviceInfo;

    @Schema(description = "네트워크 타입")
    private String networkType;

    @Schema(description = "처리 상태", example = "RECEIVED")
    private FeedbackStatus status;

    @Schema(description = "접수 시각")
    private LocalDateTime createTime;

    public static FeedbackRspDto of(Feedback f, List<String> screenshotUrls) {
        return FeedbackRspDto.builder()
                .id(f.getId())
                .reporterId(f.getReporter() == null ? null : f.getReporter().getId())
                .feedbackType(f.getFeedbackType())
                .categoryCode(f.getCategoryCode())
                .title(f.getTitle())
                .content(f.getContent())
                .screenshotUrls(screenshotUrls)
                .appVersion(f.getAppVersion())
                .deviceInfo(f.getDeviceInfo())
                .networkType(f.getNetworkType())
                .status(f.getStatus())
                .createTime(f.getCreateTime())
                .build();
    }
}
