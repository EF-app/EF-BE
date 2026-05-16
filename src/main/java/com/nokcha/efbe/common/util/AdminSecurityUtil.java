package com.nokcha.efbe.common.util;

import com.nokcha.efbe.common.auth.model.AuthAdminPrincipal;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSecurityUtil {

    public AuthAdminPrincipal getCurrentAdmin() {
        AuthAdminPrincipal p = resolveOrNull();
        if (p == null) {
            throw new BusinessException(ErrorCode.ADMIN_UNAUTHORIZED);
        }
        return p;
    }

    public Long getCurrentAdminId() {
        return getCurrentAdmin().adminId();
    }

    private AuthAdminPrincipal resolveOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthAdminPrincipal ap) return ap;
        return null;
    }
}
