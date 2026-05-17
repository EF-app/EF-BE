package com.nokcha.efbe.domain.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportRspDto {

    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportStatus status;
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
