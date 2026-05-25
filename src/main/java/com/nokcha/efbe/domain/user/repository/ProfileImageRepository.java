package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileImageRepository extends JpaRepository<UserProfileImage, Long> {

    // 회원가입 세션 기준 프로필 이미지 조회
    List<UserProfileImage> findBySignUpSessionIdOrderBySortOrderAsc(Long signUpSessionId);

    // 회원 기준 프로필 이미지 조회 (어드민 유저 상세)
    List<UserProfileImage> findByUserIdOrderBySortOrderAsc(Long userId);

    // 회원가입 세션 기준 프로필 이미지 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
