package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.CodePaymentProduct;
import com.nokcha.efbe.domain.payment.model.ProductType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "결제 상품")
public class ProductRspDto {

    @Schema(description = "상품 PK", example = "2")
    private Long productId;

    @Schema(description = "상품 코드", example = "INK_10")
    private String productCode;

    @Schema(description = "상품 유형 — INK(잉크 팩) / PALETTE(구독)", example = "INK")
    private ProductType productType;

    @Schema(description = "표시 이름", example = "잉크 10방울 패키지")
    private String name;

    @Schema(description = "판매가 (원)", example = "4500")
    private int price;

    @Schema(description = "[INK] 지급 잉크 방울. PALETTE 는 null.", example = "10", nullable = true)
    private Integer inkAmount;

    @Schema(description = "[PALETTE] 지급 기간(일). INK 는 null.", example = "30", nullable = true)
    private Integer durationDays;

    public static ProductRspDto from(CodePaymentProduct p) {
        return ProductRspDto.builder()
                .productId(p.getProductId())
                .productCode(p.getProductCode())
                .productType(p.getProductType())
                .name(p.getName())
                .price(p.getPrice())
                .inkAmount(p.getInkAmount())
                .durationDays(p.getDurationDays())
                .build();
    }
}
