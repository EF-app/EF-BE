package com.nokcha.efbe.domain.user.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.user.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 유저활동 명시 heartbeat
 *
 *  Interceptor 의 자동 갱신과 보완:
 *    - Interceptor: 능동 활동 (스와이프/액션) 시 자동
 *    - heartbeat:   화면 안 만져도 앱 켜둔 상태 보강
 *
 *  excludePathPatterns 에 등록 — Interceptor 가 한 번 더 잡아 이중 갱신하지 않도록.
 */
@Tag(name = "User Activity", description = "유저 활동 추적 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me")
public class UserActivityController {

    private final SecurityUtil securityUtil;
    private final UserActivityService recorder;
    private final Cache<Long, Boolean> userActivityThrottleCache;

    @Operation(summary = "유저 활동 시각 갱신",
            description = "FE 앱 포그라운드 동안 5분마다 호출. last_active_at = NOW() 갱신. " +
                    "Interceptor 와 같은 5분 throttle 적용 — FE 버그나 악의적 다중 호출 차단.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/heartbeat")
    public RspTemplate<Void> heartbeat() {
        Long userId = securityUtil.getCurrentUserId();
        // 5분 안 이미 갱신된 user 는 skip — FE setInterval 버그(예: 5초 호출)나 악의적 폭주 차단.
        if (userActivityThrottleCache.getIfPresent(userId) != null) {
            return new RspTemplate<>(HttpStatus.OK, "활동 시각이 이미 최신입니다.");
        }
        userActivityThrottleCache.put(userId, Boolean.TRUE);
        recorder.touch(userId);
        return new RspTemplate<>(HttpStatus.OK, "활동 시각이 갱신되었습니다.");
    }
}
