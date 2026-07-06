package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.entity.CodePaymentProduct;
import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import com.nokcha.efbe.domain.payment.model.PaymentStatus;
import com.nokcha.efbe.domain.payment.repository.CodePaymentProductRepository;
import com.nokcha.efbe.domain.payment.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 결제 오케스트레이션 — 주문 생성 → PG 승인(PAID) 시 잉크 지급 / 구독 활성화.
 *
 * 결제 시점 상품 정보는 payment_history 에 스냅샷으로 보존.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentHistoryRepository paymentRepository;
    private final CodePaymentProductRepository productRepository;
    private final InkService inkService;
    private final PaletteService paletteService;

    /** 주문 생성 (PENDING) — 상품 스냅샷 보존. PG 결제 요청 전 단계. */
    @Transactional
    public PaymentHistory createOrder(Long userId, String productCode,
                                      String paymentMethod, String pgProvider) {
        CodePaymentProduct product = productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT));

        return paymentRepository.save(PaymentHistory.builder()
                .userId(userId)
                .productId(product.getProductId())
                .productCode(product.getProductCode())
                .productName(product.getName())
                .productType(product.getProductType())
                .inkAmount(product.getInkAmount())
                .durationDays(product.getDurationDays())
                .amount(product.getPrice())
                .paymentMethod(paymentMethod)
                .pgProvider(pgProvider)
                .build());
    }

    /** PG 승인 완료 → PAID 전이 + 지급 디스패치. 멱등(이미 PAID 면 재지급 없음). */
    @Transactional
    public PaymentHistory confirmPaid(Long paymentId, String pgTid, LocalDateTime paidAt) {
        PaymentHistory payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PAYMENT));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment; // 중복 콜백 — 이미 지급됨
        }

        payment.markPaid(pgTid, paidAt);
        dispatchGrant(payment);
        return payment;
    }

    /** PG 실패 처리. */
    @Transactional
    public void markFailed(Long paymentId) {
        paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PAYMENT))
                .markFailed();
    }

    /**
     * 환불 — PAID 건만. 잉크는 잔액 회수(음수 가능성은 서비스 정책상 허용치 않음 → 향후 보강),
     * 팔레트는 자동갱신 해지 처리(부여 기간 revoke 는 향후 정교화).
     */
    @Transactional
    public void refund(Long paymentId, String reason) {
        PaymentHistory payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PAYMENT));
        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT, "환불 가능한 결제 상태가 아닙니다.");
        }
        payment.markRefunded(LocalDateTime.now());

        switch (payment.getProductType()) {
            case INK -> inkService.refund(payment.getUserId(), payment.getInkAmount(), paymentId,
                    "환불: " + reason);
            case PALETTE -> paletteService.cancel(payment.getUserId());
        }
    }

    private void dispatchGrant(PaymentHistory payment) {
        switch (payment.getProductType()) {
            case INK -> inkService.charge(payment.getUserId(), payment.getInkAmount(),
                    payment.getPaymentId(), "충전: " + payment.getProductName());
            case PALETTE -> paletteService.applyPurchase(payment.getUserId(),
                    payment.getDurationDays(), payment.getPaymentId());
        }
    }
}
