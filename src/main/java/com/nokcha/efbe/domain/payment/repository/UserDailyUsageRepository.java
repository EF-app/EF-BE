package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserDailyUsage;
import com.nokcha.efbe.domain.payment.entity.UserDailyUsageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

// 일일 사용량 카운터 레포지토리
public interface UserDailyUsageRepository extends JpaRepository<UserDailyUsage, UserDailyUsageId> {

    Optional<UserDailyUsage> findByUserIdAndUsageDateAndActionCode(
            Long userId, LocalDate usageDate, String actionCode);
}
