package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.request.BalApplyCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.BalApplyRspDto;
import com.nokcha.efbe.domain.balGame.service.BalApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BalGame Apply", description = "밸런스 게임 신청 (유저)")
@RestController
@RequestMapping("/v1/bal-applys")
@RequiredArgsConstructor
public class BalApplyController {

    private final BalApplyService balApplyService;
    private final SecurityUtil securityUtil;

    // 유저: 게임 신청
    @Operation(summary = "유저: 밸런스 게임 신청")
    @PostMapping
    public ResponseEntity<RspTemplate<BalApplyRspDto>> createApply(@Valid @RequestBody BalApplyCreateReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        BalApplyRspDto data = balApplyService.createApply(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RspTemplate<>(HttpStatus.CREATED, "신청 등록 성공", data));
    }
}
