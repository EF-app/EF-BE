package com.nokcha.efbe.domain.errorLog.repository;

import com.nokcha.efbe.domain.errorLog.entity.SystemErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

// system_error_log 표준 JPA Repository
public interface SystemErrorLogRepository extends JpaRepository<SystemErrorLog, Long> {
}
