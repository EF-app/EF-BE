package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.UserSignUpSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSignUpSessionRepository extends JpaRepository<UserSignUpSession, Long> {

    // 미완료 회원가입 세션 조회
    Optional<UserSignUpSession> findByIdAndCompletedFalse(Long id);
}
