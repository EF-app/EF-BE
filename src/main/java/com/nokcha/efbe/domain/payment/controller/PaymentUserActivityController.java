package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.PaymentLogRspDto;
import com.nokcha.efbe.domain.payment.dto.response.StarTransactionRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserInkFundRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserSubscriptionRspDto;
import com.nokcha.efbe.domain.payment.service.InkService;
import com.nokcha.efbe.domain.payment.service.PaymentService;
import com.nokcha.efbe.domain.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class PaymentUserActivityController {

    private final SecurityUtil securityUtil;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final InkService inkService;

    // 내 결제 내역
    @GetMapping("/payments")
    public RspTemplate<Page<PaymentLogRspDto>> getMyPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtil.getCurrentUserId();
        Page<PaymentLogRspDto> data = paymentService.getMyPayments(userId, page, size);
        return new RspTemplate<>(HttpStatus.OK, "결제 내역 조회 성공", data);
    }

    // 내 구독 조회
    @GetMapping("/subscriptions")
    public RspTemplate<UserSubscriptionRspDto> getMySubscription() {
        Long userId = securityUtil.getCurrentUserId();
        UserSubscriptionRspDto data = subscriptionService.getMySubscription(userId);
        return new RspTemplate<>(HttpStatus.OK, "내 구독 조회 성공", data);
    }

    // 자동 갱신 토글
    @PatchMapping("/subscription/auto-renew")
    public RspTemplate<UserSubscriptionRspDto> setAutoRenew(@RequestParam boolean enabled) {
        Long userId = securityUtil.getCurrentUserId();
        UserSubscriptionRspDto data = subscriptionService.setAutoRenew(userId, enabled);
        return new RspTemplate<>(HttpStatus.OK, "자동 갱신 설정 변경 성공", data);
    }

    // 내 잉크 잔액
    @GetMapping("/inks")
    public RspTemplate<UserInkFundRspDto> getMyBalance() {
        Long userId = securityUtil.getCurrentUserId();
        UserInkFundRspDto data = inkService.getMyBalance(userId);
        return new RspTemplate<>(HttpStatus.OK, "별 잔액 조회 성공", data);
    }

    // 내 잉크 거래 내역
    @GetMapping("/inks/transactions")
    public RspTemplate<Page<StarTransactionRspDto>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = securityUtil.getCurrentUserId();
        Page<StarTransactionRspDto> data = inkService.getTransactions(userId, page, size);
        return new RspTemplate<>(HttpStatus.OK, "별 거래 내역 조회 성공", data);
    }
}
