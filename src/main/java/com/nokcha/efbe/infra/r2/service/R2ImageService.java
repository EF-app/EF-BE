package com.nokcha.efbe.infra.r2.service;

import com.nokcha.efbe.domain.feedback.entity.Feedback;
import com.nokcha.efbe.domain.feedback.entity.FeedbackImage;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import org.springframework.web.multipart.MultipartFile;

public interface R2ImageService {

    // 프로필 이미지 업로드 (회원가입 세션 단계)
    UserProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder);

    // 프로필 이미지 업로드 (마이 프로필 수정)
    UserProfileImage uploadProfileImageForUser(MultipartFile multipartFile, String directory, Long userId, int sortOrder);

    // 피드백 첨부 이미지 업로드
    FeedbackImage uploadFeedbackImage(MultipartFile multipartFile, String directory, Feedback feedback, int sortOrder);

    // URL 로 R2 객체 삭제 (탈퇴 파기 등). 멱등 — 없는 키 삭제해도 예외 없음.
    void deleteByUrl(String url);
}
