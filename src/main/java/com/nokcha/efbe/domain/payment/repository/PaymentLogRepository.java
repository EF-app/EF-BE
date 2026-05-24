package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaymentLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

// 결제 로그 레포지토리
public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    // 주문번호 기반 조회 (멱등 처리)
    Optional<PaymentLog> findByOrderId(String orderId);

    Page<PaymentLog> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    // 유저의 결제 성공 누적 금액 (어드민 유저 상세)
    @Query("select coalesce(sum(p.amount), 0) from PaymentLog p " +
            "where p.userId = :userId and p.status = com.nokcha.efbe.domain.payment.entity.PaymentStatus.SUCCESS")
    BigDecimal sumSuccessAmountByUserId(@Param("userId") Long userId);
}
