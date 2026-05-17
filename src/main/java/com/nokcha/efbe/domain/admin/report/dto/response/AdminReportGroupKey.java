package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;

import java.time.LocalDateTime;

// Service 가 이 키들로 각 그룹의 신고 리스트를 fetch 해서 AdminReportGroupRspDto 로 합침.
public record AdminReportGroupKey(
        ReportTargetType targetType,
        Long targetId,
        Long totalCount,
        Long pendingCount,
        LocalDateTime firstReportedAt,
        LocalDateTime lastReportedAt
) {
}
