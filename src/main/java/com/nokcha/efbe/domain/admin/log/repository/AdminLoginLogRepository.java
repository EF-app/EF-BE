package com.nokcha.efbe.domain.admin.log.repository;

import com.nokcha.efbe.domain.admin.log.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AdminLoginLogRepository extends JpaRepository<AdminLoginLog, Long> {

    // 잠금 임계값 판단용: 특정 admin 의 INVALID_PASSWORD 실패가 몇 건인지
    long countByAdminIdAndFailureReasonAndLoginAtAfter(Long adminId, AdminLoginFailureReason failureReason, LocalDateTime loginAt);
}
