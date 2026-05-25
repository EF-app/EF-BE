package com.nokcha.efbe.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class InkChargeReqDto {

    @NotBlank
    private String orderId;

    @NotNull
    @Min(1)
    private Integer starAmount;

    @NotNull
    private BigDecimal amount;

    private String pgProvider;
}
