package com.nokcha.efbe.domain.log.repository;

import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {
}
