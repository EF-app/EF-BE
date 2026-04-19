package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.UserSignUpInterest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSignUpInterestRepository extends JpaRepository<UserSignUpInterest, Long> {

    // 회원가입 세션 기준 관심사 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
