package com.nokcha.efbe.domain.user.repository;

import com.nokcha.efbe.domain.user.entity.UserSignUpKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSignUpKeywordRepository extends JpaRepository<UserSignUpKeyword, Long> {

    // SessionId로 키워드 항목 찾기
    List<UserSignUpKeyword> findBySignUpSessionId(Long signUpSessionId);

    // 회원가입 세션 기준 키워드 정보 삭제
    void deleteBySignUpSessionId(Long signUpSessionId);
}
