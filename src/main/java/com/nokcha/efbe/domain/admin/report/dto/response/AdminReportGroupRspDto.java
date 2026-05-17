package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 같은 (target_type, target_id) 의 신고들을 한 그룹으로 묶은 응답.
// reports 는 시간순(오래된 → 최신). 첫 항목 = 첫 신고 = 자동 대표 후보.
@Getter
@Builder
public class AdminReportGroupRspDto {

    private ReportTargetType targetType;
    private Long targetId;
    private long totalCount;
    private long pendingCount;
    private LocalDateTime firstReportedAt;
    private LocalDateTime lastReportedAt;
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
