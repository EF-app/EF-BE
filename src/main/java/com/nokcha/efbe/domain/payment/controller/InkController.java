package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.InkBalanceRspDto;
import com.nokcha.efbe.domain.payment.dto.response.InkHistoryRspDto;
import com.nokcha.efbe.domain.payment.service.InkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Ink", description = "잉크 지갑 — 잔액/내역")
@RestController
@RequestMapping("/v1/users/me/ink")
@RequiredArgsConstructor
public class InkController {

    private final InkService inkService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "잉크 잔액 조회", description = "현재 잔액 + 누적 충전/사용.")
    @GetMapping
    public RspTemplate<InkBalanceRspDto> getBalance() {
        Long userId = securityUtil.getCurrentUserId();
        InkBalanceRspDto data = inkService.findWallet(userId)
                .map(InkBalanceRspDto::from)
                .orElseGet(InkBalanceRspDto::zero);
        return new RspTemplate<>(HttpStatus.OK, "잉크 잔액 조회 성공", data);
    }

    @Operation(summary = "잉크 내역 조회", description = "충전/사용/환불/지급 내역 (최신순).")
    @GetMapping("/history")
    public RspTemplate<List<InkHistoryRspDto>> getHistory() {
        Long userId = securityUtil.getCurrentUserId();
        List<InkHistoryRspDto> data = inkService.getHistory(userId).stream()
                .map(InkHistoryRspDto::from).toList();
        return new RspTemplate<>(HttpStatus.OK, "잉크 내역 조회 성공", data);
    }
}
