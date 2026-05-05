package com.nokcha.efbe.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 구독 결제 등록 요청 DTO
@Getter
@NoArgsConstructor
public class SubscriptionOrderReqDto {

    @NotBlank
    private String orderId;

    @NotNull
    private Integer planId;

    @NotNull
    private BigDecimal amount;

    private String pgProvider;
}
