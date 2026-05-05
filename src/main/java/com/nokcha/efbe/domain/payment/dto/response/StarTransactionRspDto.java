package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.StarTransaction;
import com.nokcha.efbe.domain.payment.entity.StarTxType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 별 거래 내역 응답 DTO
@Getter
@Builder
public class StarTransactionRspDto {
    private Long id;
    private Long userId;
    private StarTxType txType;
    private Integer amount;
    private Integer balanceAfter;
    private String refType;
    private Long refId;
    private String memo;
    private LocalDateTime createTime;

    public static StarTransactionRspDto from(StarTransaction t) {
        return StarTransactionRspDto.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .txType(t.getTxType())
                .amount(t.getAmount())
                .balanceAfter(t.getBalanceAfter())
                .refType(t.getRefType())
                .refId(t.getRefId())
                .memo(t.getMemo())
                .createTime(t.getCreateTime())
                .build();
    }
}
