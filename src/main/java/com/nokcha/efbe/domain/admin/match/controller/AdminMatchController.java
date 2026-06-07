package com.nokcha.efbe.domain.admin.match.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.match.dto.response.AdminMatchRecomputeRspDto;
import com.nokcha.efbe.domain.admin.match.service.AdminMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 매칭 운영 도구 — 관리자 전용.
 *  SecurityConfig 가 {@code /v1/admin/**} path 에 ROLE_ADMIN 강제 (다른 admin controller 와 동일 패턴).
 */
@Tag(name = "Admin Match", description = "관리자 매칭 운영 도구 (강제 재계산 등)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/matches")
public class AdminMatchController {

    private final AdminMatchService adminMatchService;

    @Operation(summary = "특정 유저 피드 강제 재계산",
            description = "어뷰즈 가드 (§10.22 throttle / 액션 임계 / 일일 횟수) 우회. " +
                    "CS 응대 / 디버깅 / 운영자 검증용. ACTIVE 상태 유저만 가능 (그 외는 read-time 오버레이로 어차피 빈 응답).")
    @PostMapping("/users/{userId}/recompute")
    public RspTemplate<AdminMatchRecomputeRspDto> recompute(@PathVariable Long userId) {
        AdminMatchRecomputeRspDto result = adminMatchService.forceRecompute(userId);
        return new RspTemplate<>(HttpStatus.OK, "피드 재계산 완료", result);
    }
}
