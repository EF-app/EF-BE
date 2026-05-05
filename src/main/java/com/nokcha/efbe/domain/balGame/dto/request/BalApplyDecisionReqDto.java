package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 신청 승인/반려 요청 DTO (관리자용)
@Getter
@NoArgsConstructor
public class BalApplyDecisionReqDto {

    @NotNull
    private BalApplyStatus status;

    @Size(max = 255)
    private String adminMemo;
}
