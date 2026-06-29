package com.nokcha.efbe.domain.admin.errorLog.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.errorLog.dto.response.AdminErrorLogDetailRspDto;
import com.nokcha.efbe.domain.admin.errorLog.dto.response.AdminErrorLogPageRspDto;
import com.nokcha.efbe.domain.admin.errorLog.service.AdminErrorLogService;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Admin ErrorLog", description = "관리자 시스템 에러 로그 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/error-logs")
public class AdminErrorLogController {

    private final AdminErrorLogService adminErrorLogService;

    @Operation(summary = "에러 로그 목록 조회",
            description = "source / severity / errorType(LIKE) / userId / adminId / resolved / 기간(from~to) 동적 필터. " +
                    "발생 최신순. totalPages 포함 숫자 페이지네이션.")
    @GetMapping
    public RspTemplate<AdminErrorLogPageRspDto> getErrorLogs(
            @RequestParam(required = false) ErrorSource source,
            @RequestParam(required = false) ErrorSeverity severity,
            @RequestParam(required = false) String errorType,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) Boolean resolved,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return new RspTemplate<>(HttpStatus.OK, "에러 로그 목록을 조회했습니다.",
                adminErrorLogService.getErrorLogs(source, severity, errorType, userId, adminId, resolved, from, to, page, size));
    }

    @Operation(summary = "에러 로그 단건 상세", description = "stacktrace·metadata 전체 포함.")
    @GetMapping("/{id}")
    public RspTemplate<AdminErrorLogDetailRspDto> getErrorLog(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "에러 로그 상세를 조회했습니다.",
                adminErrorLogService.getErrorLog(id));
    }

    @Operation(summary = "에러 로그 해결 처리", description = "resolved_at 을 현재 시각으로 세팅. 멱등.")
    @PatchMapping("/{id}/resolve")
    public RspTemplate<AdminErrorLogDetailRspDto> resolveErrorLog(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "에러 로그를 해결 처리했습니다.",
                adminErrorLogService.resolveErrorLog(id));
    }
}
