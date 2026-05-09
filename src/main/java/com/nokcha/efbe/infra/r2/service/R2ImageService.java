package com.nokcha.efbe.infra.r2.service;

import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import org.springframework.web.multipart.MultipartFile;

public interface R2ImageService {

    // 프로필 이미지 업로드
    UserProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder);

    // 피드백 첨부 이미지 업로드
    FeedbackImage uploadFeedbackImage(MultipartFile multipartFile, Feedback feedback, int sortOrder);
}
