package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.InkHistory;
import com.nokcha.efbe.domain.payment.model.InkTxType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "잉크 원장 내역 1건")
public class InkHistoryRspDto {

    @Schema(description = "원장 PK", example = "5001")
    private Long inkHistoryId;

    @Schema(description = "거래 유형 — CHARGE/USE/REFUND/GRANT", example = "USE")
    private InkTxType txType;

    @Schema(description = "변동량 (부호 델타, +충전/-사용)", example = "-2")
    private int amount;

    @Schema(description = "거래 직후 잔액", example = "8")
    private int balanceAfter;

    @Schema(description = "[USE] 사용 아이템 코드. 그 외 null.", example = "super_like", nullable = true)
    private String itemCode;

    @Schema(description = "비고 (충전 상품명, 지급 사유 등)", example = "슈퍼좋아요", nullable = true)
    private String description;

    @Schema(description = "거래 시각", example = "2026-07-07T12:34:56")
    private LocalDateTime createdAt;

    public static InkHistoryRspDto from(InkHistory h) {
        return InkHistoryRspDto.builder()
                .inkHistoryId(h.getInkHistoryId())
                .txType(h.getTxType())
                .amount(h.getAmount())
                .balanceAfter(h.getBalanceAfter())
                .itemCode(h.getItemCode())
                .description(h.getDescription())
                .createdAt(h.getCreateTime())
                .build();
    }
}
