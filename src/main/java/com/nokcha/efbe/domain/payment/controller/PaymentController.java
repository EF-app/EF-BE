package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.request.InkChargeReqDto;
import com.nokcha.efbe.domain.payment.dto.request.SubscriptionOrderReqDto;
import com.nokcha.efbe.domain.payment.dto.response.PaymentLogRspDto;
import com.nokcha.efbe.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    // 잉크 충전 결제 확정
    @PostMapping("/ink-charges")
    public RspTemplate<PaymentLogRspDto> confirmInkCharge(@Valid @RequestBody InkChargeReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        PaymentLogRspDto data = paymentService.confirmInkCharge(userId, req);
        return new RspTemplate<>(HttpStatus.CREATED, "잉크 충전 확정 성공", data);
    }

    // 구독 결제 확정
    @PostMapping("/subscriptions")
    public RspTemplate<PaymentLogRspDto> confirmSubscription(@Valid @RequestBody SubscriptionOrderReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        PaymentLogRspDto data = paymentService.confirmSubscription(userId, req);
        return new RspTemplate<>(HttpStatus.CREATED, "구독 결제 확정 성공", data);
    }
}
