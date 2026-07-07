package com.nokcha.efbe.domain.payment.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.payment.dto.response.SubscriptionRspDto;
import com.nokcha.efbe.domain.payment.service.PaletteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Subscription", description = "팔레트 구독 — 상태/해지/재활성")
@RestController
@RequestMapping("/v1/users/me/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final PaletteService paletteService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내 구독 상태 조회", description = "프리미엄 여부 / 만료일 / 자동갱신 / 해지시각.")
    @GetMapping
    public RspTemplate<SubscriptionRspDto> getStatus() {
        Long userId = securityUtil.getCurrentUserId();
        SubscriptionRspDto data = paletteService.findStatus(userId)
                .map(p -> SubscriptionRspDto.from(p, LocalDateTime.now()))
                .orElseGet(SubscriptionRspDto::free);
        return new RspTemplate<>(HttpStatus.OK, "구독 상태 조회 성공", data);
    }

    @Operation(summary = "구독 자동갱신 해지", description = "만료일까지는 프리미엄 유지, 자동갱신만 끈다.")
    @PostMapping("/cancel")
    public RspTemplate<Void> cancel() {
        paletteService.cancel(securityUtil.getCurrentUserId());
        return new RspTemplate<>(HttpStatus.OK, "구독 해지 성공");
    }

    @Operation(summary = "구독 해지 철회", description = "자동갱신을 다시 켠다.")
    @PostMapping("/reactivate")
    public RspTemplate<Void> reactivate() {
        paletteService.reactivate(securityUtil.getCurrentUserId());
        return new RspTemplate<>(HttpStatus.OK, "구독 재활성 성공");
    }
}
