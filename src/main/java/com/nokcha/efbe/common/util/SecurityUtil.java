package com.nokcha.efbe.common.util;

import com.nokcha.efbe.common.auth.model.AuthUserPrincipal;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
// 새 admin 권한 체크는 AdminSecurityUtil (common/util) 사용.
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private static final long SYSTEM_USER_ID = 0L;

    // private final AdminRepository adminRepository;

    // 현재 인증된 유저 ID (없으면 401)
    public Long getCurrentUserId() {
        Long id = resolveUserIdOrNull();
        if (id == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return id;
    }

    // 현재 유저 ID (미인증 시 시스템 ID 반환 - Auditor 전용)
    public Long getCurrentUserIdOrSystem() {
        Long id = resolveUserIdOrNull();
        return id == null ? SYSTEM_USER_ID : id;
    }

    // 현재 유저 ID (미인증 시 null) - permitAll 엔드포인트가 viewer 컨텍스트를 옵션으로 받을 때 사용
    public Long getCurrentUserIdOrNull() {
        return resolveUserIdOrNull();
    }

    public String getCurrentLoginId() {
        Authentication authentication = getAuthentication();
        String loginId = authentication.getName();

        if (loginId == null || loginId.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        return loginId;
    }

    /*
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
    */

    // SecurityContext 에서 principal 을 Long(userId) 으로 해석. 미인증/anonymous 면 null.
    private Long resolveUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthUserPrincipal aup && aup.getUserId() != null) {
            return aup.getUserId();
        }
        return null;
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorCode.INVALID_USER);
        }

        return authentication;
    }
}
