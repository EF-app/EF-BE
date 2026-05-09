package com.nokcha.efbe.domain.feedback.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.feedback.dto.request.FeedbackCreateReqDto;
import com.nokcha.efbe.domain.feedback.dto.response.FeedbackRspDto;
import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackCategoryCode;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.feedback.entity.FeedbackType;
import com.nokcha.efbe.domain.feedback.repository.FeedbackRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.infra.r2.service.R2ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

// 피드백 등록 서비스
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final R2ImageService r2ImageService;

    @Transactional
    public FeedbackRspDto createFeedback(Long reporterId, FeedbackCreateReqDto req, List<MultipartFile> images) {
        // 카테고리-유형 조합 검증
        FeedbackType type = req.getFeedbackType();
        FeedbackCategoryCode category = req.getCategoryCode();
        if (!type.allows(category)) {
            throw new BusinessException(ErrorCode.INVALID_FEEDBACK_CATEGORY);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        Feedback saved = feedbackRepository.save(Feedback.builder()
                .reporter(reporter)
                .feedbackType(type)
                .categoryCode(category)
                .title(req.getTitle())
                .content(req.getContent())
                .appVersion(req.getAppVersion())
                .deviceInfo(req.getDeviceInfo())
                .networkType(req.getNetworkType())
                .build());

        List<FeedbackImage> savedImages = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile img : images) {
                if (img == null || img.isEmpty()) continue;
                savedImages.add(r2ImageService.uploadFeedbackImage(img, saved, order++));
            }
        }

        return FeedbackRspDto.of(saved, savedImages);
    }
}
