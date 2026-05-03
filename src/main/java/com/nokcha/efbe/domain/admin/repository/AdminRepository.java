package com.nokcha.efbe.domain.admin.repository;

import com.nokcha.efbe.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 로그인 아이디로 관리자 조회
    Optional<Admin> findByLoginId(String loginId);

    // 로그인 아이디 존재 여부 조회
    boolean existsByLoginId(String loginId);
}
