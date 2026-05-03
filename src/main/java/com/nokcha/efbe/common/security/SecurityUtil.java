package com.nokcha.efbe.common.security;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// JWT 인증 컨텍스트에서 현재 유저 ID를 꺼내는 유틸
public final class SecurityUtil {

    private static final long SYSTEM_USER_ID = 0L;

    private SecurityUtil() {
    }

    // 현재 인증된 유저 ID (없으면 401)
    // SecurityContext 가 비어있다는 건 SecurityConfig 가 막았어야 할 요청이 흘러왔다는 뜻이므로 401 로 차단.
    public static Long getCurrentUserId() {
        Long id = resolveUserIdOrNull();
        if (id == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return id;
    }

    // 현재 유저 ID (미인증 시 시스템 ID 반환 - Auditor 전용)
    public static Long getCurrentUserIdOrSystem() {
        Long id = resolveUserIdOrNull();
        return id == null ? SYSTEM_USER_ID : id;
    }

    // SecurityContext 에서 principal 을 Long 으로 해석
    private static Long resolveUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) return null;
        if (principal instanceof Long l) return l;
        if (principal instanceof Number n) return n.longValue();
        String name = authentication.getName();
        try {
            return Long.parseLong(name);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
