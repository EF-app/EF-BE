package com.nokcha.efbe.domain.payment.repository;

import com.nokcha.efbe.domain.payment.entity.PaymentDailySummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

// 재무 정산 일별 집계 레포지토리 (배치 갱신, 관리자 대시보드 조회)
public interface PaymentDailySummaryRepository extends JpaRepository<PaymentDailySummary, LocalDate> {
}
