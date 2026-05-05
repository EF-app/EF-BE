package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.AdRewardLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

// 광고 보상 로그 레포지토리
public interface AdRewardLogRepository extends JpaRepository<AdRewardLog, Long> {

    // 멱등 체크 (광고 SDK 트랜잭션 ID 기준)
    boolean existsByAdTxId(String adTxId);

    // 일일 보상 횟수 (상한 체크)
    long countByUserIdAndRewardDateAndRewardType(Long userId, LocalDate rewardDate, String rewardType);
}
