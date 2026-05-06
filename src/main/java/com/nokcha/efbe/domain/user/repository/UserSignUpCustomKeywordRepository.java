package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserSignUpCustomKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSignUpCustomKeywordRepository extends JpaRepository<UserSignUpCustomKeyword, Long> {

    // 회원가입 세션 기준 커스텀 키워드를 조회
    List<UserSignUpCustomKeyword> findBySignUpSessionId(Long signUpSessionId);

    // 회원가입 세션 기준 커스텀 키워드를 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
