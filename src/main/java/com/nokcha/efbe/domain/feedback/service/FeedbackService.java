package com.nokcha.efbe.domain.feedback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.feedback.dto.request.FeedbackCreateReqDto;
import com.nokcha.efbe.domain.feedback.dto.response.FeedbackRspDto;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.feedback.repository.FeedbackRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

// 피드백 등록 서비스
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public FeedbackRspDto createFeedback(Long reporterId, FeedbackCreateReqDto req) {
        // 카테고리-유형 조합 검증
        FeedbackType type = req.getFeedbackType();
        FeedbackCategoryCode category = req.getCategoryCode();
        if (!type.allows(category)) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_CATEGORY);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String screenshotUrlsJson = serializeScreenshotUrls(req.getScreenshotUrls());

        Feedback saved = feedbackRepository.save(Feedback.builder()
                .reporter(reporter)
                .feedbackType(type)
                .categoryCode(category)
                .title(req.getTitle())
                .content(req.getContent())
                .screenshotUrls(screenshotUrlsJson)
                .appVersion(req.getAppVersion())
                .deviceInfo(req.getDeviceInfo())
                .networkType(req.getNetworkType())
                .build());

        List<String> screenshotsForResponse = req.getScreenshotUrls() == null
                ? Collections.emptyList()
                : req.getScreenshotUrls();
        return FeedbackRspDto.of(saved, screenshotsForResponse);
    }

    private String serializeScreenshotUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    // 응답에서 다시 List<String> 으로 풀고 싶을 때 사용 (목록/상세 API 추가 시)
    @SuppressWarnings("unused")
    private List<String> deserializeScreenshotUrls(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
