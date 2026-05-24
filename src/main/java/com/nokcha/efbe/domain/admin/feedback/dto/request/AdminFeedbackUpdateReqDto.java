package com.nokcha.efbe.domain.admin.feedback.dto.request;

import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 어드민 피드백 처리 요청
@Getter
@NoArgsConstructor
@Schema(description = "어드민 피드백 처리 요청")
public class AdminFeedbackUpdateReqDto {

    @Schema(description = "처리 상태", example = "IN_PROGRESS")
    private FeedbackStatus status;

    @Schema(description = "유저에게 보낼 답변 (선택)")
    private String adminReply;

    @Size(max = 1000)
    @Schema(description = "내부 메모 (유저 비공개, 최대 1000자)")
    private String adminInternalMemo;
}
