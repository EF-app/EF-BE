package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserSignUpCustomInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSignUpCustomInterestRepository extends JpaRepository<UserSignUpCustomInterest, Long> {

    // 회원가입 세션 기준 커스텀 관심사를 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
