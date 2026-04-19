package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    // 회원가입 세션 기준 프로필 이미지 조회
    List<ProfileImage> findBySignUpSessionIdOrderBySortOrderAsc(Long signUpSessionId);

    // 회원가입 세션 기준 프로필 이미지 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
