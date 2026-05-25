package com.nokcha.efbe.domain.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "신고 접수 결과 — 유저 측 신고 등록 응답")
public class ReportRspDto {

    @Schema(description = "신고 PK", example = "1024")
    private Long id;

    @Schema(description = "신고 대상 유형", example = "POST_IT")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK", example = "42")
    private Long targetId;

    @Schema(description = "처리 상태 (접수 직후엔 항상 PENDING)", example = "PENDING")
    private ReportStatus status;

    @Schema(description = "신고 접수 시각", example = "2026-05-25T18:42:00")
    private LocalDateTime createTime;

    public static ReportRspDto from(Report report) {
        return ReportRspDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .status(report.getStatus())
                .createTime(report.getCreateTime())
                .build();
    }
}
