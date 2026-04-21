package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserSignUpPersonal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSignUpPersonalRepository extends JpaRepository<UserSignUpPersonal, Long> {
    // SessionId로 스타일 항목 찾기
    List<UserSignUpPersonal> findBySignUpSessionId(Long signUpSessionId);

    // 회원가입 세션 기준 성향 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
