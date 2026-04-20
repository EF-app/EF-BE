package com.efbe.domain.user.repository;

import com.efbe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 로그인 아이디로 사용자 조회
    Optional<User> findByLoginId(String loginId);

    // 로그인 아이디 존재 여부 조회
    boolean existsByLoginId(String loginId);

    // 휴대폰 번호 존재 여부 조회
    boolean existsByPhone(String phone);

    // 닉네임 존재 여부 조회
    boolean existsByNickname(String nickname);
}
