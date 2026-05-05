package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.UserMonthlyUsage;
import com.nokcha.efbe.domain.payment.entity.UserMonthlyUsageId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

// 프리미엄 월간 무료 사용 카운터 레포지토리
public interface UserMonthlyUsageRepository extends JpaRepository<UserMonthlyUsage, UserMonthlyUsageId> {

    Optional<UserMonthlyUsage> findByUserIdAndPeriodStartAndFeatureCode(
            Long userId, LocalDate periodStart, String featureCode);
}
