package com.nokcha.efbe.domain.log.repository;

import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    // 어드민 유저 상세 — 최근 접속 이력 20건
    List<UserLoginLog> findTop20ByUserIdOrderByLoginAtDesc(Long userId);
}
