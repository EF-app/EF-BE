package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.payment.dto.request.RcWebhookReqDto;
import com.nokcha.efbe.domain.payment.service.RevenueCatEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RevenueCat webhook 수신 — 스토어 결제/구독 이벤트 → 지급. JWT 대신 Authorization 헤더 시크릿으로 보호.
 * 시크릿은 application.yml 의 {@code revenuecat.webhook-auth} (RevenueCat 대시보드에 동일 값 설정).
 */
@Tag(name = "Payment Webhook", description = "RevenueCat 인앱결제 webhook")
@RestController
@RequestMapping("/v1/payments/revenuecat")
@RequiredArgsConstructor
public class RevenueCatWebhookController {

    private final RevenueCatEventService eventService;

    @Value("${revenuecat.webhook-auth:}")
    private String webhookAuth;

    @Operation(summary = "RevenueCat webhook", description = "스토어 결제/구독 이벤트 수신 → 잉크/구독 지급. 멱등 처리.")
    @PostMapping("/webhook")
    public RspTemplate<Void> webhook(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody RcWebhookReqDto req) {
        if (webhookAuth != null && !webhookAuth.isBlank() && !webhookAuth.equals(authorization)) {
            throw new BusinessException("RevenueCat webhook 인증 실패", HttpStatus.UNAUTHORIZED);
        }
        eventService.handle(req.event());
        return new RspTemplate<>(HttpStatus.OK, "ok");
    }
}
