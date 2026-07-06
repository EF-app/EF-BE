package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    /** PG 콜백 멱등 — 중복 승인 방어. */
    Optional<PaymentHistory> findByPgTid(String pgTid);

    boolean existsByPgTid(String pgTid);

    List<PaymentHistory> findByUserIdOrderByCreateTimeDesc(Long userId);
}
