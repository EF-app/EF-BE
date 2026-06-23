package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 전체 정상 배치 강제 실행 결과 — POST /v1/admin/matches/batch/full 응답
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "전체 매칭 정상 배치 강제 실행 결과 (04:00 cron 과 동일)")
public class AdminMatchFullBatchRspDto {

    @Schema(description = "처리 대상 활성 viewer 총수", example = "7875")
    private int totalViewers;

    @Schema(description = "정상 처리된 viewer 수", example = "7870")
    private int successCount;

    @Schema(description = "처리 실패한 viewer 수", example = "5")
    private int failCount;

    @Schema(description = "전체 처리 시간 (ms)", example = "184321")
    private long durationMs;
}
