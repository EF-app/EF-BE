package com.nokcha.efbe.domain.admin.auth.repository;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    // 로그인 아이디로 관리자 조회
    Optional<AdminAccount> findByLoginId(String loginId);

    // 로그인 아이디 존재 여부 조회
    boolean existsByLoginId(String loginId);
}
