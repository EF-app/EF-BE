package com.nokcha.efbe.domain.admin.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.balGame.dto.request.AdminBalApplyRejectReqDto;
import com.nokcha.efbe.domain.admin.balGame.service.AdminBalApplyService;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin BalApply", description = "관리자 밸런스 게임 신청 처리 (목록·거절)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/bal-apply")
public class AdminBalApplyController {

    private final AdminBalApplyService adminBalApplyService;

    @Operation(summary = "신청 목록 조회",
            description = "status 필터 (생략 시 전체). 기본 createTime DESC.")
    @GetMapping
    public RspTemplate<Page<BalApplyRspDto>> getApplies(
            @RequestParam(required = false) BalApplyStatus status,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "신청 목록 조회 성공",
                adminBalApplyService.getApplies(status, pageable));
    }

    @Operation(summary = "신청 거절",
            description = "PENDING 신청을 REJECTED 로 변경. adminMemo 에 거절 사유 기록. " +
                    "이미 APPROVED/REJECTED 처리된 신청은 400.")
    @PatchMapping("/{applyId}/reject")
    public RspTemplate<BalApplyRspDto> rejectApply(
            @PathVariable Long applyId,
            @Valid @RequestBody AdminBalApplyRejectReqDto req
    ) {
        return new RspTemplate<>(HttpStatus.OK, "신청이 거절되었습니다.",
                adminBalApplyService.rejectApply(applyId, req));
    }
}
