package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.SubscriptionPlanRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserSubscriptionRspDto;
import com.nokcha.efbe.domain.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 구독 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 플랜 목록
    @GetMapping("/plans")
    public ResponseEntity<RspTemplate<List<SubscriptionPlanRspDto>>> getPlans() {
        List<SubscriptionPlanRspDto> data = subscriptionService.getPlans();
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "구독 플랜 조회 성공", data));
    }

    // 내 구독 조회
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<UserSubscriptionRspDto>> getMySubscription() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserSubscriptionRspDto data = subscriptionService.getMySubscription(userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내 구독 조회 성공", data));
    }

    // 자동 갱신 토글
    @PatchMapping("/me/auto-renew")
    public ResponseEntity<RspTemplate<UserSubscriptionRspDto>> setAutoRenew(@RequestParam boolean enabled) {
        Long userId = SecurityUtil.getCurrentUserId();
        UserSubscriptionRspDto data = subscriptionService.setAutoRenew(userId, enabled);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "자동 갱신 설정 변경 성공", data));
    }
}
