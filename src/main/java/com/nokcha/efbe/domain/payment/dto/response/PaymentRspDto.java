package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.PaymentHistory;
import com.nokcha.efbe.domain.payment.model.PaymentStatus;
import com.nokcha.efbe.domain.payment.model.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "결제 내역")
public class PaymentRspDto {

    @Schema(description = "결제 PK", example = "1001")
    private Long paymentId;

    @Schema(description = "상품 코드 스냅샷", example = "INK_10")
    private String productCode;

    @Schema(description = "상품명 스냅샷", example = "잉크 10방울 패키지")
    private String productName;

    @Schema(description = "상품 유형", example = "INK")
    private ProductType productType;

    @Schema(description = "결제 금액 (원)", example = "4500")
    private int amount;

    @Schema(description = "결제 상태 — PENDING/PAID/FAILED/CANCELED/REFUNDED", example = "PAID")
    private PaymentStatus status;

    @Schema(description = "결제 완료 시각. 미결제면 null.", example = "2026-07-07T12:34:56", nullable = true)
    private LocalDateTime paidAt;

    @Schema(description = "주문 생성 시각", example = "2026-07-07T12:30:00")
    private LocalDateTime createdAt;

    public static PaymentRspDto from(PaymentHistory p) {
        return PaymentRspDto.builder()
                .paymentId(p.getPaymentId())
                .productCode(p.getProductCode())
                .productName(p.getProductName())
                .productType(p.getProductType())
                .amount(p.getAmount())
                .status(p.getStatus())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreateTime())
                .build();
    }
}
