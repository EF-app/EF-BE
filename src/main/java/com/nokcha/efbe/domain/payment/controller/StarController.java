package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.StarTransactionRspDto;
import com.nokcha.efbe.domain.payment.dto.response.UserStarBalanceRspDto;
import com.nokcha.efbe.domain.payment.service.StarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 별 재화 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/stars")
@RequiredArgsConstructor
public class StarController {

    private final StarService starService;

    // 내 별 잔액
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<UserStarBalanceRspDto>> getMyBalance() {
        Long userId = SecurityUtil.getCurrentUserId();
        UserStarBalanceRspDto data = starService.getMyBalance(userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "별 잔액 조회 성공", data));
    }

    // 내 거래 내역
    @GetMapping("/me/transactions")
    public ResponseEntity<RspTemplate<Page<StarTransactionRspDto>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<StarTransactionRspDto> data = starService.getTransactions(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "별 거래 내역 조회 성공", data));
    }
}
