package com.nokcha.efbe.domain.admin.report.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.report.dto.ReportGroupSort;
import com.nokcha.efbe.domain.admin.report.dto.request.AdminReportProcessReqDto;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportDetailRspDto;
import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportGroupRspDto;
import com.nokcha.efbe.domain.admin.report.service.AdminReportService;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Report", description = "관리자 신고 처리 (목록·상세·처리·기각)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "신고 목록 조회 (그룹화)",
            description = "(target_type, target_id) 단위로 묶은 목록. 같은 그룹의 모든 PENDING 에 동일 suspension_id" +
                    "파라미터 이름이 'sort' 가 아닌 'groupSort'")
    @GetMapping("/grouped")
    public RspTemplate<Page<AdminReportGroupRspDto>> getReportsGrouped(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(name = "groupSort", required = false, defaultValue = "OLDEST")
            ReportGroupSort groupSort,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "신고 그룹 목록을 조회했습니다.",
                adminReportService.getReportsGrouped(status, groupSort, pageable));
    }

    @Operation(summary = "신고 단건 상세 조회")
    @GetMapping("/{reportId}")
    public RspTemplate<AdminReportDetailRspDto> getReport(@PathVariable Long reportId) {
        return new RspTemplate<>(HttpStatus.OK, "신고를 조회했습니다.",
                adminReportService.getReport(reportId));
    }

    @Operation(summary = "신고 처리 (PROCESSED)",
            description = "제재로 이어진 경우 suspensionId 를 함께 보냅니다. " +
                    "같은 target 의 가장 오래된 PENDING 신고가 자동으로 대표로 선정, 나머지는 cascade 일괄 처리")
    @PostMapping("/{reportId}/process")
    public RspTemplate<AdminReportDetailRspDto> processReport(
            @PathVariable Long reportId,
            @Valid @RequestBody AdminReportProcessReqDto reqDto
    ) {
        return new RspTemplate<>(HttpStatus.OK, "신고가 처리되었습니다.",
                adminReportService.processReport(reportId, reqDto));
    }

    @Operation(summary = "신고 기각 (DISMISSED)",
            description = "제재 사유 부족 등으로 신고를 기각합니다. 이미 처리된 신고는 409.")
    @PostMapping("/{reportId}/dismiss")
    public RspTemplate<AdminReportDetailRspDto> dismissReport(@PathVariable Long reportId) {
        return new RspTemplate<>(HttpStatus.OK, "신고가 기각되었습니다.",
                adminReportService.dismissReport(reportId));
    }
}
