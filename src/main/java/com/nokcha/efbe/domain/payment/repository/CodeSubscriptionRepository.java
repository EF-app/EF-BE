package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.CodeSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 구독 플랜 마스터 레포지토리
public interface CodeSubscriptionRepository extends JpaRepository<CodeSubscription, Integer> {

    Optional<CodeSubscription> findByPlanCode(String planCode);

    List<CodeSubscription> findByIsActiveTrueOrderByPriceAsc();
}
