package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// Service 가 이 키들로 각 그룹의 신고 리스트를 fetch 해서 AdminReportGroupRspDto 로 합침.
// 내부 데이터 캐리어 — API 응답으로 직접 노출되지 않음.
@Schema(description = "(targetType, targetId) 신고 그룹 키 — 내부 집계용. AdminReportGroupRspDto 로 합쳐져 응답됨",
        hidden = true)
public record AdminReportGroupKey(
        @Schema(description = "신고 대상 유형", example = "POST_IT")
        ReportTargetType targetType,

        @Schema(description = "신고 대상 PK", example = "42")
        Long targetId,

        @Schema(description = "이 (target_type, target_id) 의 전체 신고 수", example = "5")
        Long totalCount,

        @Schema(description = "이 그룹의 PENDING 신고 수", example = "2")
        Long pendingCount,

        @Schema(description = "그룹 내 첫 신고 시각 (자동 대표 후보)", example = "2026-05-23T10:00:00")
        LocalDateTime firstReportedAt,

        @Schema(description = "그룹 내 마지막 신고 시각", example = "2026-05-24T18:30:00")
        LocalDateTime lastReportedAt
) {
}
