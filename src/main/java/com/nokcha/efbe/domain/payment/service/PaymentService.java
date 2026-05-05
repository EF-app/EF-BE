package com.nokcha.efbe.domain.payment.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.dto.request.StarChargeReqDto;
import com.nokcha.efbe.domain.payment.dto.request.SubscriptionOrderReqDto;
import com.nokcha.efbe.domain.payment.dto.response.PaymentLogRspDto;
import com.nokcha.efbe.domain.payment.entity.PaymentLog;
import com.nokcha.efbe.domain.payment.entity.PaymentType;
import com.nokcha.efbe.domain.payment.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 결제 서비스 (멱등 키 기반 - 주문번호)
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentLogRepository paymentLogRepository;
    private final StarService starService;
    private final SubscriptionService subscriptionService;

    // 별 충전 결제 성공 처리 (PG 콜백 훅)
    @Transactional
    public PaymentLogRspDto confirmStarCharge(Long userId, StarChargeReqDto req) {
        if (paymentLogRepository.findByOrderId(req.getOrderId()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
        }
        PaymentLog log = paymentLogRepository.save(PaymentLog.builder()
                .userId(userId).orderId(req.getOrderId()).paymentType(PaymentType.STAR_CHARGE)
                .starAmount(req.getStarAmount()).amount(req.getAmount()).pgProvider(req.getPgProvider())
                .build());
        log.markSuccess();
        starService.charge(userId, req.getStarAmount(), "PAYMENT", log.getId(), "star-charge:" + req.getOrderId());
        return PaymentLogRspDto.from(log);
    }

    // 구독 결제 성공 처리 (PG 콜백 훅)
    @Transactional
    public PaymentLogRspDto confirmSubscription(Long userId, SubscriptionOrderReqDto req) {
        if (paymentLogRepository.findByOrderId(req.getOrderId()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_PAYMENT);
        }
        PaymentLog log = paymentLogRepository.save(PaymentLog.builder()
                .userId(userId).orderId(req.getOrderId()).paymentType(PaymentType.SUBSCRIPTION)
                .refPlanId(req.getPlanId()).amount(req.getAmount()).pgProvider(req.getPgProvider())
                .build());
        log.markSuccess();
        subscriptionService.startOrRenew(userId, req.getPlanId());
        return PaymentLogRspDto.from(log);
    }

    // 내 결제 내역 조회
    @Transactional(readOnly = true)
    public Page<PaymentLogRspDto> getMyPayments(Long userId, int page, int size) {
        return paymentLogRepository.findByUserIdOrderByCreateTimeDesc(userId, PageRequest.of(page, size))
                .map(PaymentLogRspDto::from);
    }
}
