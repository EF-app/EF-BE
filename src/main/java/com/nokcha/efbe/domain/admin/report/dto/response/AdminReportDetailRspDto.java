package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// effectiveSuspensionId : 화면 표시용 합성값. cascade 신고에서도 parent.suspensionId 가 보임.
// parentReportId        : 대표 신고의 id. cascade 신고에서만 채워짐, 대표 자기 자신은 null.
@Getter
@Builder
@Schema(description = "어드민 신고 단건 상세 — cascade 신고는 effectiveSuspensionId/parentReportId 로 대표 신고 정보 합성")
public class AdminReportDetailRspDto {

    @Schema(description = "신고 PK", example = "1024")
    private Long id;

    @Schema(description = "신고 대상 유형 (포스트잇/밸런스댓글/프로필/채팅/채팅이미지)",
            example = "POST_IT")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK (target_type 에 따라 의미 다름)", example = "42")
    private Long targetId;

    @Schema(description = "신고자 유저 PK (탈퇴 시 null)", example = "17", nullable = true)
    private Long reporterId;

    @Schema(description = "다중 선택 사유 코드",
            example = "HATE,SPAM",
            nullable = true)
    private String reasonCodes;

    @Schema(description = "신고자가 자유 입력한 상세 텍스트",
            example = "욕설이 반복적으로 포함되어 있습니다.",
            nullable = true)
    private String detail;

    @Schema(description = "처리 상태 (PENDING / PROCESSED / DISMISSED)", example = "PROCESSED")
    private ReportStatus status;

    @Schema(description = "처리한 관리자 PK", example = "1", nullable = true)
    private Long adminProcessedById;

    @Schema(description = "처리한 관리자 이름", example = "관리자", nullable = true)
    private String adminProcessedByName;

    @Schema(description = "처리 시각", example = "2026-05-24T10:00:00", nullable = true)
    private LocalDateTime adminProcessedAt;

    @Schema(description = "이 신고 자체가 연결된 suspension_log.id (자기 자신이 대표일 때만)",
            example = "37", nullable = true)
    private Long suspensionId;

    @Schema(description = "유효 suspension_log.id — cascade 신고도 parent.suspensionId 로 채워짐",
            example = "37", nullable = true)
    private Long effectiveSuspensionId;

    @Schema(description = "대표 신고의 PK — cascade 신고에서만 채워짐. 대표 자기 자신은 null",
            example = "1020", nullable = true)
    private Long parentReportId;

    @Schema(description = "신고 접수 시각", example = "2026-05-23T18:42:00")
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
