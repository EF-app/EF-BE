package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.PaymentLog;
import com.nokcha.efbe.domain.payment.entity.PaymentStatus;
import com.nokcha.efbe.domain.payment.entity.PaymentType;
import com.nokcha.efbe.domain.payment.entity.RefundType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 결제 로그 응답 DTO
@Getter
@Builder
public class PaymentLogRspDto {
    private Long id;
    private String orderId;
    private PaymentType paymentType;
    private Integer refPlanId;
    private Integer starAmount;
    private BigDecimal amount;
    private String currency;
    private String pgProvider;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private RefundType refundType;
    private String refundReason;
    private LocalDateTime createTime;

    public static PaymentLogRspDto from(PaymentLog p) {
        return PaymentLogRspDto.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .paymentType(p.getPaymentType())
                .refPlanId(p.getRefPlanId())
                .starAmount(p.getStarAmount())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .pgProvider(p.getPgProvider())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .refundedAt(p.getRefundedAt())
                .refundType(p.getRefundType())
                .refundReason(p.getRefundReason())
                .createTime(p.getCreateTime())
                .build();
    }
}
