package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyDecisionReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalGameSummaryRspDto;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.service.BalApplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 밸런스 게임 신청/승인 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/bal-apply")
@RequiredArgsConstructor
public class BalApplyController {

    private final BalApplyService balApplyService;

    // 유저: 게임 신청
    @PostMapping
    public ResponseEntity<RspTemplate<BalApplyRspDto>> createApply(@Valid @RequestBody BalApplyCreateReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        BalApplyRspDto data = balApplyService.createApply(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "신청 등록 성공", data));
    }

    // 관리자: 신청 목록
    @GetMapping
    public ResponseEntity<RspTemplate<Page<BalApplyRspDto>>> getApplies(
            @RequestParam(required = false) BalApplyStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<BalApplyRspDto> data = balApplyService.getApplies(status, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "신청 목록 조회 성공", data));
    }

    // 유저: 내 신청 목록
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<Page<BalApplyRspDto>>> getMyApplies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<BalApplyRspDto> data = balApplyService.getMyApplies(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내 신청 목록 조회 성공", data));
    }

    // 관리자: 승인/반려. 승인 시 BalGame DRAFT 생성됨
    @PatchMapping("/{applyId}/decision")
    public ResponseEntity<RspTemplate<BalGameSummaryRspDto>> decideApply(
            @PathVariable Long applyId,
            @Valid @RequestBody BalApplyDecisionReqDto req) {
        BalGameSummaryRspDto data = balApplyService.decideApply(applyId, req);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "신청 처리 성공", data));
    }
}
