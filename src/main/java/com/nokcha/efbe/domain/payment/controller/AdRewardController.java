package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.request.AdRewardReqDto;
import com.nokcha.efbe.domain.payment.service.AdRewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 광고 보상 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/ad-rewards")
@RequiredArgsConstructor
public class AdRewardController {

    private final AdRewardService adRewardService;

    // 광고 보상 수령 (ad_tx_id 기반 멱등)
    @PostMapping
    public ResponseEntity<RspTemplate<Void>> claim(@Valid @RequestBody AdRewardReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        adRewardService.claim(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "광고 보상 수령 성공"));
    }
}
