package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.ItemUsageHistory;
import com.nokcha.efbe.domain.payment.model.UsageSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemUsageHistoryRepository extends JpaRepository<ItemUsageHistory, Long> {

    List<ItemUsageHistory> findByUserIdAndItemCodeAndCreateTimeBetweenOrderByCreateTimeDesc(
            Long userId, String itemCode, LocalDateTime from, LocalDateTime to);

    /** 기간 내 특정 재원(무료/유료) 사용 건수 — 정산/분석. */
    long countByUserIdAndItemCodeAndSourceAndCreateTimeBetween(
            Long userId, String itemCode, UsageSource source, LocalDateTime from, LocalDateTime to);
}
