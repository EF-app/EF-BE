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

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final R2ImageService r2ImageService;

    @Transactional
    public FeedbackRspDto createFeedback(Long reporterId, FeedbackCreateReqDto req, List<MultipartFile> images) {
        req.validate();

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

        List<FeedbackImage> savedImages = uploadImages(images, saved);

        return FeedbackRspDto.of(saved, savedImages);
    }

    private List<FeedbackImage> uploadImages(List<MultipartFile> images, Feedback saved) {
        List<FeedbackImage> result = new ArrayList<>();

        if (images == null || images.isEmpty()) return result;

        int order = 0;
        for (MultipartFile img : images) {
            if (img == null || img.isEmpty()) continue;
            result.add(r2ImageService.uploadFeedbackImage(img, "feedback", saved, order++));
        }

        return result;
    }
}
