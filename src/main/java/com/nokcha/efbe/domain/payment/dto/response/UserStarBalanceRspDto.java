package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.UserStarBalance;
import lombok.Builder;
import lombok.Getter;

// 별 잔액 응답 DTO
@Getter
@Builder
public class UserStarBalanceRspDto {
    private Long userId;
    private Integer balance;
    private Integer totalCharged;
    private Integer totalUsed;

    public static UserStarBalanceRspDto from(UserStarBalance b) {
        return UserStarBalanceRspDto.builder()
                .userId(b.getUserId())
                .balance(b.getBalance())
                .totalCharged(b.getTotalCharged())
                .totalUsed(b.getTotalUsed())
                .build();
    }
}
