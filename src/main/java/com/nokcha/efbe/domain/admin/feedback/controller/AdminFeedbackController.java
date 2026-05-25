package com.nokcha.efbe.domain.admin.feedback.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.feedback.dto.request.AdminFeedbackUpdateReqDto;
import com.nokcha.efbe.domain.admin.feedback.dto.response.AdminFeedbackRspDto;
import com.nokcha.efbe.domain.admin.feedback.service.AdminFeedbackService;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackStatus;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Feedback", description = "관리자 피드백 (버그신고/기능요청) 관리")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/feedbacks")
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    @Operation(summary = "피드백 목록 조회", description = "feedbackType / status / categoryCode / keyword(제목·내용 LIKE) 동적 필터. 최신순.")
    @GetMapping
    public RspTemplate<Page<AdminFeedbackRspDto>> getFeedbacks(
            @RequestParam(required = false) FeedbackType feedbackType,
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) FeedbackCategoryCode categoryCode,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 15, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "피드백 목록을 조회했습니다.", adminFeedbackService.getFeedbacks(feedbackType, status, categoryCode, keyword, pageable));
    }

    @Operation(summary = "피드백 단건 상세", description = "첨부 이미지를 포함하여 피드백을 단건 조회합니다.")
    @GetMapping("/{id}")
    public RspTemplate<AdminFeedbackRspDto> getFeedback(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "피드백 상세를 조회했습니다.", adminFeedbackService.getFeedback(id));
    }

    @Operation(summary = "피드백 처리", description = "상태/답변/내부메모 갱신. 처리한 관리자가 담당자로 지정됨. null 필드는 변경 안 함.")
    @PatchMapping("/{id}")
    public RspTemplate<AdminFeedbackRspDto> updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody AdminFeedbackUpdateReqDto req
    ) {
        return new RspTemplate<>(HttpStatus.OK, "피드백이 처리되었습니다.", adminFeedbackService.updateFeedback(id, req));
    }
}
