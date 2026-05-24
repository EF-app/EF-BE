package com.nokcha.efbe.domain.admin.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 신고 처리
@Getter
@NoArgsConstructor
@Schema(description = "신고 처리 요청 — 제재로 이어진 경우 suspension_log.id 를 함께 전달")
public class AdminReportProcessReqDto {

    @Positive
    @Schema(description = "연결할 suspension_log.id (제재 발동된 경우만, 미발동 시 null/생략 가능)",
            example = "37",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            nullable = true)
    private Long suspensionId;
}
