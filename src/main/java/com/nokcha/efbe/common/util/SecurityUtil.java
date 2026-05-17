package com.nokcha.efbe.common.util;

import com.nokcha.efbe.common.auth.model.AuthUserPrincipal;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private static final long SYSTEM_USER_ID = 0L;

    public Long getCurrentUserId() {
        Long id = resolveUserIdOrNull();
        if (id == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return id;
    }

    public Long getCurrentUserIdOrSystem() {
        Long id = resolveUserIdOrNull();
        return id == null ? SYSTEM_USER_ID : id;
    }

    public Long getCurrentUserIdOrNull() {
        return resolveUserIdOrNull();
    }

    private Long resolveUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUserPrincipal authUserPrincipal && authUserPrincipal.getUserId() != null) {
            return authUserPrincipal.getUserId();
        }

        return null;
    }
}
