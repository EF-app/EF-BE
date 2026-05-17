package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

//   - effectiveSuspensionId  : 화면 표시용 합성값. cascade 신고에서도 parent.suspensionId 가 보임.
//   - parentReportId         : 대표 신고의 id. cascade 신고에서만 채워짐, 대표 자기 자신은 null.
@Getter
@Builder
public class AdminReportDetailRspDto {

    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private Long reporterId;
    // 다중 선택 사유 코드 (콤마 구분, 예: "HATE,SPAM"). 신고자가 선택한 사유 칩.
    private String reasonCodes;
    // 신고자가 자유 입력한 상세 텍스트.
    private String detail;
    private ReportStatus status;
    private Long adminProcessedById;
    private String adminProcessedByName;
    private LocalDateTime adminProcessedAt;
    private Long suspensionId;
    private Long effectiveSuspensionId;
    private Long parentReportId;
    private LocalDateTime createTime;

    // 단순 매핑 — 자기 자신이 대표인 경우 (또는 PENDING/DISMISSED).
    public static AdminReportDetailRspDto from(Report report) {
        return buildBase(report)
                .effectiveSuspensionId(report.getSuspensionId())
                .parentReportId(null)
                .build();
    }

    // 합성 매핑 — cascade 신고용. Service 가 parent FK dereference 로 가져온 값을 전달.
    public static AdminReportDetailRspDto from(Report report,
                                                Long effectiveSuspensionId,
                                                Long parentReportId) {
        return buildBase(report)
                .effectiveSuspensionId(effectiveSuspensionId)
                .parentReportId(parentReportId)
                .build();
    }

    private static AdminReportDetailRspDtoBuilder buildBase(Report report) {
        return AdminReportDetailRspDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reporterId(report.getReporter() == null ? null : report.getReporter().getId())
                .reasonCodes(report.getReasonCodes())
                .detail(report.getDetail())
                .status(report.getStatus())
                .adminProcessedById(report.getAdminProcessedBy() == null ? null : report.getAdminProcessedBy().getId())
                .adminProcessedByName(report.getAdminProcessedBy() == null ? null : report.getAdminProcessedBy().getName())
                .adminProcessedAt(report.getAdminProcessedAt())
                .suspensionId(report.getSuspensionId())
                .createTime(report.getCreateTime());
    }
}
