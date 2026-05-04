package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.request.StarChargeReqDto;
import com.nokcha.efbe.domain.payment.dto.request.SubscriptionOrderReqDto;
import com.nokcha.efbe.domain.payment.dto.response.PaymentLogRspDto;
import com.nokcha.efbe.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 결제 RESTful 컨트롤러 (PG 성공 콜백 훅)
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // 별 충전 결제 확정
    @PostMapping("/star-charges")
    public ResponseEntity<RspTemplate<PaymentLogRspDto>> confirmStarCharge(
            @Valid @RequestBody StarChargeReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        PaymentLogRspDto data = paymentService.confirmStarCharge(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "별 충전 확정 성공", data));
    }

    // 구독 결제 확정
    @PostMapping("/subscriptions")
    public ResponseEntity<RspTemplate<PaymentLogRspDto>> confirmSubscription(
            @Valid @RequestBody SubscriptionOrderReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        PaymentLogRspDto data = paymentService.confirmSubscription(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "구독 결제 확정 성공", data));
    }

    // 내 결제 내역
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<Page<PaymentLogRspDto>>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PaymentLogRspDto> data = paymentService.getMyPayments(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "결제 내역 조회 성공", data));
    }
}
