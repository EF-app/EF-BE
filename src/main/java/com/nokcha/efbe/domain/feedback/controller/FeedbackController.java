package com.nokcha.efbe.domain.feedback.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.feedback.dto.request.FeedbackCreateReqDto;
import com.nokcha.efbe.domain.feedback.dto.response.FeedbackRspDto;
import com.nokcha.efbe.domain.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 피드백(버그신고/기능요청) RESTful 컨트롤러 — 사용자 등록
@Tag(name = "Feedback", description = "피드백 — 버그신고/기능요청")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 피드백 등록 (현재 로그인 유저가 신고자)
    @Operation(summary = "피드백 등록 (버그신고/기능요청)",
            description = "feedbackType 은 BUG 또는 FEATURE_REQUEST. categoryCode 는 유형별 허용 목록 안에서 선택 — " +
                    "BUG: UI_BROKEN/FEATURE_NOT_WORK/PERFORMANCE/PAYMENT/NOTIFICATION/CHAT, " +
                    "FEATURE_REQUEST: NEW_FEATURE/UX_DESIGN/PERF_IMPROVE/PAYMENT/NOTIFICATION/CHAT. " +
                    "screenshotUrls 는 R2 업로드 후 URL 배열로 전달 (옵션).")
    @PostMapping
    public ResponseEntity<RspTemplate<FeedbackRspDto>> createFeedback(@Valid @RequestBody FeedbackCreateReqDto req) {
        Long reporterId = SecurityUtil.getCurrentUserId();
        FeedbackRspDto data = feedbackService.createFeedback(reporterId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "피드백 등록 성공", data));
    }
}
