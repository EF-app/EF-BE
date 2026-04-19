package com.efbe.infra.r2.service;

import com.efbe.domain.user.entity.ProfileImage;
import org.springframework.web.multipart.MultipartFile;

public interface R2ImageService {

    // 프로필 이미지 업로드
    ProfileImage uploadProfileImage(MultipartFile multipartFile, String directory, Long signUpSessionId, int sortOrder);
}
