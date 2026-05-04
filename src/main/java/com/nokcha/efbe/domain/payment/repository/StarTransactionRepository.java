package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.StarTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 별 거래 내역 레포지토리
public interface StarTransactionRepository extends JpaRepository<StarTransaction, Long> {

    Page<StarTransaction> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}
