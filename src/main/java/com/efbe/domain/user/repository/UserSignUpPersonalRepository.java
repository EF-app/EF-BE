package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.UserSignUpPersonal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSignUpPersonalRepository extends JpaRepository<UserSignUpPersonal, Long> {

    // 회원가입 세션 기준 성향 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
