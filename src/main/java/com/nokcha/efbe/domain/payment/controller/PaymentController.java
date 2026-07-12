package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.PaymentRspDto;
import com.nokcha.efbe.domain.payment.dto.response.ProductRspDto;
import com.nokcha.efbe.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Payment", description = "결제 — 상품 목록/내역 (구매·지급은 스토어 IAP + RevenueCat webhook)")
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "결제 상품 목록", description = "판매 중인 잉크 팩/구독 상품 (sort_order 순). FE 는 표시용, 실제 결제는 스토어 IAP.")
    @GetMapping("/products")
    public RspTemplate<List<ProductRspDto>> getProducts() {
        List<ProductRspDto> data = paymentService.getActiveProducts().stream()
                .map(ProductRspDto::from).toList();
        return new RspTemplate<>(HttpStatus.OK, "결제 상품 목록 조회 성공", data);
    }

    @Operation(summary = "내 결제 내역", description = "로그인 유저의 결제 내역 (최신순).")
    @GetMapping("/me")
    public RspTemplate<List<PaymentRspDto>> getMyPayments() {
        Long userId = securityUtil.getCurrentUserId();
        List<PaymentRspDto> data = paymentService.getMyPayments(userId).stream()
                .map(PaymentRspDto::from).toList();
        return new RspTemplate<>(HttpStatus.OK, "결제 내역 조회 성공", data);
    }
}
