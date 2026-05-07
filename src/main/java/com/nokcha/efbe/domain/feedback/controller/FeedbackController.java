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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// 피드백(버그신고/기능요청) RESTful 컨트롤러 — 사용자 등록
@Tag(name = "Feedback", description = "피드백 — 버그신고/기능요청")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 피드백 등록 (현재 로그인 유저가 신고자)
    @Operation(summary = "피드백 등록 (버그신고/기능요청)",
            description = "multipart/form-data 로 전송. " +
                    "\"data\" 파트(application/json) — FeedbackCreateReqDto. " +
                    "\"images\" 파트(application/octet-stream 등) — 첨부 이미지 0..N 개 (옵션). " +
                    "이미지 정책: 최대 5MB/장, 확장자 jpg/jpeg/png/gif/webp/heic/heif/bmp, 개수 제한 없음. " +
                    "feedbackType 은 BUG 또는 FEATURE_REQUEST. categoryCode 는 유형별 허용 목록 안에서 선택 — " +
                    "BUG: UI_BROKEN/FEATURE_NOT_WORK/PERFORMANCE/PAYMENT/NOTIFICATION/CHAT/ETC, " +
                    "FEATURE_REQUEST: NEW_FEATURE/UX_DESIGN/PERF_IMPROVE/PAYMENT/NOTIFICATION/CHAT/ETC.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RspTemplate<FeedbackRspDto>> createFeedback(
            @Valid @RequestPart("data") FeedbackCreateReqDto data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        Long reporterId = SecurityUtil.getCurrentUserId();
        FeedbackRspDto result = feedbackService.createFeedback(reporterId, data, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "피드백 등록 성공", result));
    }
}
