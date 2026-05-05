package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaymentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 결제 로그 레포지토리
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    // 주문번호 기반 조회 (멱등 처리)
    Optional<PaymentLog> findByOrderId(String orderId);

    Page<PaymentLog> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}
