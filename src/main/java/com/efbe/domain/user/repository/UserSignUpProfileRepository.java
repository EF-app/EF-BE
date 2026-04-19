package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.UserSignUpProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSignUpProfileRepository extends JpaRepository<UserSignUpProfile, Long> {

    // 회원가입 세션 기준 프로필 정보 조회
    Optional<UserSignUpProfile> findBySignUpSessionId(Long signUpSessionId);

    // 회원가입 세션 기준 프로필 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
