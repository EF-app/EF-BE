package com.nokcha.efbe.domain.feedback.controller;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Feedback", description = "피드백 — 버그신고/기능요청")
@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final SecurityUtil securityUtil;

    // 피드백 등록 (현재 로그인 유저가 신고자)
    @Operation(summary = "피드백 등록 (버그신고/기능요청)",
            description = "feedbackType 은 BUG 또는 FEATURE_REQUEST. categoryCode 는 유형별 허용 목록 안에서 선택")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RspTemplate<FeedbackRspDto> createFeedback(
            @RequestPart("feedbackType") String feedbackTypeRaw,
            @RequestPart("categoryCode") String categoryCodeRaw,
            @RequestPart("title") String title,
            @RequestPart("content") String content,
            @RequestPart(value = "appVersion", required = false) String appVersion,
            @RequestPart(value = "deviceInfo", required = false) String deviceInfo,
            @RequestPart(value = "networkType", required = false) String networkType,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {

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

        FeedbackCreateReqDto reqDto = FeedbackCreateReqDto.builder()
                .feedbackType(feedbackType)
                .categoryCode(categoryCode)
                .title(title)
                .content(content)
                .appVersion(appVersion)
                .deviceInfo(deviceInfo)
                .networkType(networkType)
                .build();

        Long reporterId = securityUtil.getCurrentUserId();

        FeedbackRspDto result = feedbackService.createFeedback(reporterId, reqDto, images);

        return new RspTemplate<>(HttpStatus.CREATED, "피드백 등록 성공", result);
    }
}
