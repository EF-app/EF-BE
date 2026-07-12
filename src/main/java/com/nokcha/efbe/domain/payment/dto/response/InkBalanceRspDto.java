package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.InkWallet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "잉크 지갑 잔액")
public class InkBalanceRspDto {

    @Schema(description = "현재 잔액 (방울)", example = "8")
    private int balance;

    @Schema(description = "누적 충전 (방울)", example = "20")
    private int totalCharged;

    @Schema(description = "누적 사용 (방울)", example = "12")
    private int totalUsed;

    public static InkBalanceRspDto from(InkWallet w) {
        return InkBalanceRspDto.builder()
                .balance(w.getBalance())
                .totalCharged(w.getTotalCharged())
                .totalUsed(w.getTotalUsed())
                .build();
    }

    /** 지갑이 아직 없는 유저 — 전부 0. */
    public static InkBalanceRspDto zero() {
        return InkBalanceRspDto.builder().balance(0).totalCharged(0).totalUsed(0).build();
    }
}
