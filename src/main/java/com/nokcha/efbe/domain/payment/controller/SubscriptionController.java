package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.payment.dto.response.SubscriptionPlanRspDto;
import com.nokcha.efbe.domain.payment.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // 플랜 목록
    @GetMapping("/plans")
    public RspTemplate<List<SubscriptionPlanRspDto>> getPlans() {
        List<SubscriptionPlanRspDto> data = subscriptionService.getPlans();
        return new RspTemplate<>(HttpStatus.OK, "구독 플랜 조회 성공", data);
    }
}
