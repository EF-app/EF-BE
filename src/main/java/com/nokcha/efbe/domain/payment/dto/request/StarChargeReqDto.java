package com.nokcha.efbe.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 별 충전 결제 등록 요청 DTO (PG 성공 웹훅/클라이언트 콜백)
@Getter
@NoArgsConstructor
public class StarChargeReqDto {

    @NotBlank
    private String orderId;

    @NotNull
    @Min(1)
    private Integer starAmount;

    @NotNull
    private BigDecimal amount;

    private String pgProvider;
}
