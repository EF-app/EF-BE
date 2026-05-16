package com.nokcha.efbe.domain.admin.auth.repository;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {
    Optional<AdminAccount> findByLoginId(String loginId);

    // 회원가입 시 user/admin 간 loginId 충돌 방지용
    boolean existsByLoginId(String loginId);
}
