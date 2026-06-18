package com.nokcha.efbe.common.interceptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.user.service.UserActivityRecorder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 컨트롤러 정상 처리 후 호출 — last_active_at 갱신을 throttle + async 로 처리.
 *
 *  흐름 (postHandle):
 *    1. SecurityUtil.getCurrentUserIdOrNull() — 비인증 요청은 null → skip
 *    2. throttleCache.getIfPresent(userId)    — 5분 안 이미 갱신 → skip
 *    3. cache.put(userId, TRUE)               — 갱신 마킹
 *    4. recorder.touch(userId)                — @Async 라 즉시 반환
 *
 *  실패 / 예외 발생 시 컨트롤러는 postHandle 호출 안 함
 *  비인증/제재 user 는 SecurityFilterChain 이 미리 막아서 이 단계 도달 안 함.
 *
 *  대상 / 제외 경로는 {@code WebMvcConfig} 에서 excludePathPatterns 로 관리.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityInterceptor implements HandlerInterceptor {

    private final SecurityUtil securityUtil;
    private final Cache<Long, Boolean> userActivityThrottleCache;
    private final UserActivityRecorder recorder;

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        Long userId = securityUtil.getCurrentUserIdOrNull();
        if (userId == null) return;
        if (userActivityThrottleCache.getIfPresent(userId) != null) return;
        userActivityThrottleCache.put(userId, Boolean.TRUE);
        recorder.touch(userId);
    }
}
