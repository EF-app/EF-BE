package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 같은 (target_type, target_id) 의 신고들을 한 그룹으로 묶은 응답.
// reports 는 시간순(오래된 → 최신). 첫 항목 = 첫 신고 = 자동 대표 후보.
@Getter
@Builder
@Schema(description = "어드민 신고 그룹 응답 — (targetType, targetId) 단위로 묶은 신고 묶음. 처리 시 첫 신고가 자동 대표")
public class AdminReportGroupRspDto {

    @Schema(description = "신고 대상 유형", example = "POST_IT")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK", example = "42")
    private Long targetId;

    @Schema(description = "이 그룹의 전체 신고 수", example = "5")
    private long totalCount;

    @Schema(description = "이 그룹의 PENDING 신고 수", example = "2")
    private long pendingCount;

    @Schema(description = "그룹 내 첫 신고 시각 (자동 대표 후보)", example = "2026-05-23T10:00:00")
    private LocalDateTime firstReportedAt;

    @Schema(description = "그룹 내 마지막 신고 시각", example = "2026-05-24T18:30:00")
    private LocalDateTime lastReportedAt;

    @Schema(description = "그룹에 속한 신고 리스트 (시간순: 오래된 → 최신). 첫 항목이 자동 대표 후보")
    private List<AdminReportSummaryRspDto> reports;

    public static AdminReportGroupRspDto of(AdminReportGroupKey key, List<AdminReportSummaryRspDto> reports) {
        return AdminReportGroupRspDto.builder()
                .targetType(key.targetType())
                .targetId(key.targetId())
                .totalCount(key.totalCount())
                .pendingCount(key.pendingCount())
                .firstReportedAt(key.firstReportedAt())
                .lastReportedAt(key.lastReportedAt())
                .reports(reports)
                .build();
    }
}
