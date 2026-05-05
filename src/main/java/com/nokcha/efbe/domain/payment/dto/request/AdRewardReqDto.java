package com.nokcha.efbe.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 광고 보상 요청 DTO (멱등성 adTxId 필수)
@Getter
@NoArgsConstructor
public class AdRewardReqDto {

    @NotBlank
    private String rewardType;

    @NotBlank
    private String adTxId;

    @NotNull
    private Integer rewardAmount;

    private String adNetwork;
}
