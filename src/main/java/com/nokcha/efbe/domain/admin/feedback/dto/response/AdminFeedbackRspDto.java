package com.nokcha.efbe.domain.admin.feedback.dto.response;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "어드민 피드백 응답 (목록/상세 공용)")
public class AdminFeedbackRspDto {

    @Schema(description = "피드백 PK", example = "15")
    private Long id;

    @Schema(description = "신고자 유저 PK", example = "2")
    private Long reporterId;

    @Schema(description = "신고자 닉네임", example = "밍닝")
    private String reporterNickname;

    @Schema(description = "신고자 로그인 아이디", example = "test002")
    private String reporterLoginId;

    @Schema(description = "피드백 유형", example = "BUG")
    private FeedbackType feedbackType;

    @Schema(description = "카테고리 코드", example = "PAYMENT")
    private FeedbackCategoryCode categoryCode;

    @Schema(description = "제목", example = "결제가 실패했어요")
    private String title;

    @Schema(description = "내용", example = "결제 버튼을 눌러도 반응이 없습니다.")
    private String content;

    @Schema(description = "첨부 스크린샷 URL 목록 (목록 조회 시에는 빈 배열)")
    private List<String> screenshotUrls;

    @Schema(description = "앱 버전", example = "1.0.0")
    private String appVersion;

    @Schema(description = "디바이스 정보", example = "Android 35")
    private String deviceInfo;

    @Schema(description = "네트워크 타입", example = "WIFI")
    private String networkType;

    @Schema(description = "처리 상태", example = "IN_PROGRESS")
    private FeedbackStatus status;

    @Schema(description = "유저에게 보낼 답변 (없으면 null)")
    private String adminReply;

    @Schema(description = "답변 등록 일시 (답변이 없으면 null)")
    private LocalDateTime adminReplyAt;

    @Schema(description = "담당 관리자 PK", example = "1")
    private Long adminHandlerId;

    @Schema(description = "담당 관리자 이름", example = "관리자")
    private String adminHandlerName;

    @Schema(description = "내부 메모 (유저 비공개)")
    private String adminInternalMemo;

    @Schema(description = "피드백 접수 일시")
    private LocalDateTime createTime;

    @Schema(description = "최종 수정 일시")
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
