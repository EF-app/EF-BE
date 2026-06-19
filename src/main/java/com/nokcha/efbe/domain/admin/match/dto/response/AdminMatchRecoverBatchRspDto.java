package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 보정 배치 강제 실행 결과 — POST /v1/admin/matches/batch/recover 응답
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "보정 배치 실행 결과")
public class AdminMatchRecoverBatchRspDto {

    @Schema(description = "보정 대상 viewer 수 (오늘 daily_feed row 없는 활성 viewer)", example = "12")
    private int targetCount;

    @Schema(description = "정상 흐름으로 복구된 viewer 수", example = "10")
    private int recoverCount;

    @Schema(description = "ColdStartFeed fallback 으로 복구된 viewer 수", example = "1")
    private int coldStartCount;

    @Schema(description = "ColdStartFeed 마저 실패해 미복구된 viewer 수", example = "1")
    private int failCount;

    @Schema(description = "전체 처리 시간 (ms)", example = "8421")
    private long durationMs;
}
