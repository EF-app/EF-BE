package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByUserIdOrderByCreateTimeDesc(Long userId);

    /** 스토어 거래 멱등 보조 — 동일 거래 재기록 방어. */
    boolean existsByStoreTransactionId(String storeTransactionId);
}
