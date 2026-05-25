package com.nokcha.efbe.domain.payment.dto.response;

import com.nokcha.efbe.domain.payment.entity.UserInkFund;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInkFundRspDto {
    @Schema(description = "유저 PK", example = "1")
    private Long userId;

    @Schema(description = "잔액", example = "2")
    private Integer fund;

    @Schema(description = "총 충전 금액", example = "30")
    private Integer totalCharged;

    @Schema(description = "총 사용량", example = "30")
    private Integer totalUsed;

    public static UserInkFundRspDto from(UserInkFund userInkFund) {
        return UserInkFundRspDto.builder()
                .userId(userInkFund.getUserId())
                .fund(userInkFund.getFund())
                .totalCharged(userInkFund.getTotalCharged())
                .totalUsed(userInkFund.getTotalUsed())
                .build();
    }
}
