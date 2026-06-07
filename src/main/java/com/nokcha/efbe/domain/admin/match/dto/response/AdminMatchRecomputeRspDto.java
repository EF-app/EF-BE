package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 강제 재계산 결과 — POST /v1/admin/matches/users/{userId}/recompute 응답
@Schema(description = "강제 피드 재계산 결과")
public record AdminMatchRecomputeRspDto(
        @Schema(description = "대상 유저 id", example = "1234")
        long userId,

        @Schema(description = "재계산 후 daily_feed row 수 (reserved 자리 제외)", example = "45")
        int cardCount,

        @Schema(description = "처리 시간 (ms)", example = "287")
        long durationMs
) {}
