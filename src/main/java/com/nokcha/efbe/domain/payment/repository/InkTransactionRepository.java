package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.InkTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InkTransactionRepository extends JpaRepository<InkTransaction, Long> {

    Page<InkTransaction> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}
