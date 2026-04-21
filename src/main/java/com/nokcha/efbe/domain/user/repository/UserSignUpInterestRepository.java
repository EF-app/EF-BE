package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserSignUpInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSignUpInterestRepository extends JpaRepository<UserSignUpInterest, Long> {
    // SessionId로 관심사 항목 찾기
    List<UserSignUpInterest> findBySignUpSessionId(Long signUpSessionId);

    // 회원가입 세션 기준 관심사 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
