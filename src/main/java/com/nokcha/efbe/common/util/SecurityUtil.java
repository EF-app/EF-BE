package com.nokcha.efbe.common.util;

import com.nokcha.efbe.common.auth.model.AuthUserPrincipal;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final AdminRepository adminRepository;

    public Long getCurrentUserId() {
        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof AuthUserPrincipal authUserPrincipal && authUserPrincipal.getUserId() != null) {
            return authUserPrincipal.getUserId();
        }

        throw new BusinessException(ErrorCode.INVALID_USER);
    }

    public String getCurrentLoginId() {
        Authentication authentication = getAuthentication();
        String loginId = authentication.getName();

        if (loginId == null || loginId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        return loginId;
    }

    public Admin getAdmin(String loginId) {
        return adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));
    }

    public void validateAdmin(String loginId) {
        getAdmin(loginId);
    }

    public void validateCurrentAdmin() {
        validateAdmin(getCurrentLoginId());
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        return authentication;
    }
}
