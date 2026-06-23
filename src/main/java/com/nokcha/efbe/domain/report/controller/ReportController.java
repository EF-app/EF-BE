package com.nokcha.efbe.domain.report.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.report.dto.request.ReportCreateReqDto;
import com.nokcha.efbe.domain.report.dto.response.ReportRspDto;
import com.nokcha.efbe.domain.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "신고 (다형성: 포스트잇/밸런스 댓글/프로필/채팅/채팅 이미지)")
@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "신고 등록",
            description = "target_type 과 target_id 로 다른 사용자의 콘텐츠/프로필을 신고합니다. " +
                    "같은 대상 중복 신고와 PROFILE 자기 신고는 차단됩니다.")
    @PostMapping
    public RspTemplate<ReportRspDto> create(@Valid @RequestBody ReportCreateReqDto req) {
        Long reporterId = securityUtil.getCurrentUserId();
        ReportRspDto data = reportService.createReport(reporterId, req);
        return new RspTemplate<>(HttpStatus.CREATED, "신고가 접수되었습니다.", data);
    }
}
