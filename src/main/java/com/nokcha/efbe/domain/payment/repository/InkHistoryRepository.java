package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.InkHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InkHistoryRepository extends JpaRepository<InkHistory, Long> {

    List<InkHistory> findByUserIdOrderByCreateTimeDesc(Long userId);

    /** 원장 합산 = 잔액. ink_wallet 캐시 검증/복구용. */
    @Query("SELECT COALESCE(SUM(h.amount), 0) FROM InkHistory h WHERE h.userId = :userId")
    int sumBalance(@Param("userId") Long userId);
}
