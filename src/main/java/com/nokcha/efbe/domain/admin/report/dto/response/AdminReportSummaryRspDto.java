package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// admin 측 신고 목록 항목. 리스트 화면용.
// enrich 필드 (reporterNickname, targetUserId, targetUserNickname, balGameId, targetPreview) 는 모두 옵션 — null 허용.
// 그룹화 응답(getReportsGrouped) 에서만 채워지고, 플랫 응답(from(Report)) 에선 null.
@Getter
@Builder
public class AdminReportSummaryRspDto {

    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportStatus status;
    private Long reporterId;
    private LocalDateTime createTime;

    // enrich 필드 (옵션)
    private String reporterNickname;
    private Long targetUserId;
    private String targetUserNickname;
    private Long balGameId;
    private String targetPreview;

    // 단순 매핑 — enrich 없음 (플랫 응답용).
    public static AdminReportSummaryRspDto from(Report report) {
        return AdminReportSummaryRspDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .status(report.getStatus())
                .reporterId(report.getReporter() == null ? null : report.getReporter().getId())
                .createTime(report.getCreateTime())
                .build();
    }
}
