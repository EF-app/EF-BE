package com.nokcha.efbe.domain.admin.report.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PROCESSED 처리 요청 — 제재로 이어졌으면 suspension_log.id 연결
@Getter
@NoArgsConstructor
public class AdminReportProcessReqDto {

    @Positive
    private Long suspensionId;
}
