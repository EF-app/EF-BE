package com.nokcha.efbe.domain.feedback.controller;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.feedback.dto.request.FeedbackCreateReqDto;
import com.nokcha.efbe.domain.feedback.dto.response.FeedbackRspDto;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.feedback.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
// multipart/form-data — 회원가입 프로필(/v1/users/signup/profile) 과 동일하게 필드별 @RequestPart 사용.
// FE 는 form.append("title", ...) 처럼 평문 파트로 보내고 images 만 파일 파트.
@Tag(name = "Feedback", description = "피드백 — 버그신고/기능요청")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    // 피드백 등록 (현재 로그인 유저가 신고자)
    @Operation(summary = "피드백 등록 (버그신고/기능요청)",
            description = "multipart/form-data 로 전송 (회원가입 프로필 패턴과 동일). " +
                    "각 필드를 평문 @RequestPart 로 전송하고 images 만 파일 파트. " +
                    "이미지 정책: 최대 5MB/장, 확장자 jpg/jpeg/png/gif/webp/heic/heif/bmp, 개수 제한 없음. " +
                    "feedbackType 은 BUG 또는 FEATURE_REQUEST. categoryCode 는 유형별 허용 목록 안에서 선택 — " +
                    "BUG: UI_BROKEN/FEATURE_NOT_WORK/PERFORMANCE/PAYMENT/NOTIFICATION/CHAT/ETC, " +
                    "FEATURE_REQUEST: NEW_FEATURE/UX_DESIGN/PERF_IMPROVE/PAYMENT/NOTIFICATION/CHAT/ETC.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RspTemplate<FeedbackRspDto>> createFeedback(
            @RequestPart("feedbackType") String feedbackTypeRaw,
            @RequestPart("categoryCode") String categoryCodeRaw,
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "appVersion", required = false) String appVersion,
            @RequestPart(value = "deviceInfo", required = false) String deviceInfo,
            @RequestPart(value = "networkType", required = false) String networkType,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        // 평문 파트 → enum 파싱 + 기본 검증.
        // (DTO @Valid 대신 컨트롤러 진입부에서 일괄 처리 — multipart 평문 파트는 @Valid 가 안 걸림)
        if (title == null || title.isBlank() || title.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (appVersion != null && appVersion.length() > 30) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (deviceInfo != null && deviceInfo.length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (networkType != null && networkType.length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        FeedbackType feedbackType;
        FeedbackCategoryCode categoryCode;
        try {
            feedbackType = FeedbackType.valueOf(feedbackTypeRaw);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        try {
            categoryCode = FeedbackCategoryCode.valueOf(categoryCodeRaw);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_CATEGORY);
        }

        FeedbackCreateReqDto data = FeedbackCreateReqDto.of(
                feedbackType, categoryCode, title, content, appVersion, deviceInfo, networkType);

        Long reporterId = SecurityUtil.getCurrentUserId();
        FeedbackRspDto result = feedbackService.createFeedback(reporterId, data, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "피드백 등록 성공", result));
    }
}
