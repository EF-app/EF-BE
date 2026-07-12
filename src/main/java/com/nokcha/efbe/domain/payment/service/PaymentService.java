package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.domain.payment.entity.CodePaymentProduct;
import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import com.nokcha.efbe.domain.payment.repository.CodePaymentProductRepository;
import com.nokcha.efbe.domain.payment.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 조회 — 상품 목록 / 내 결제 내역. 지급(충전/구독)은 RevenueCat webhook({@link RevenueCatEventService}) 담당.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentHistoryRepository paymentRepository;
    private final CodePaymentProductRepository productRepository;

    /** 판매 중인 상품 목록. */
    @Transactional(readOnly = true)
    public List<CodePaymentProduct> getActiveProducts() {
        return productRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    /** 내 결제 내역 (최신순). */
    @Transactional(readOnly = true)
    public List<PaymentHistory> getMyPayments(Long userId) {
        return paymentRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }
}
