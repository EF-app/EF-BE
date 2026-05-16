package com.nokcha.efbe.domain.admin.auth.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.dto.request.AdminLoginReqDto;
import com.nokcha.efbe.domain.admin.auth.dto.request.AdminRefreshReqDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminLoginRspDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminSummaryDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminTokenRspDto;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.auth.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.common.auth.jwt.AdminJwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final AdminAccountRepository adminAccountRepository;
    private final AdminLoginLogService adminLoginLogService;
    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 아이디·비밀번호 검증 후 access + refresh 토큰 발급.
    // 모든 분기에서 admin_login_log 기록 (실패 로그/잠금 갱신은 REQUIRES_NEW 로 외부 롤백과 분리).
    @Transactional
    public AdminLoginRspDto login(AdminLoginReqDto reqDto, HttpServletRequest request) {
        String loginId = reqDto.getLoginId();
        String ip = resolveClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        LocalDateTime now = LocalDateTime.now();

        AdminAccount admin = adminAccountRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    adminLoginLogService.recordFailure(loginId, null, AdminLoginFailureReason.INVALID_ID, ip, userAgent);
                    return new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
                });

        if (!admin.isActive()) {
            adminLoginLogService.recordFailure(loginId, admin.getId(), AdminLoginFailureReason.ACCOUNT_INACTIVE, ip, userAgent);
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_DISABLED);
        }

        if (admin.isLocked(now)) {
            adminLoginLogService.recordFailure(loginId, admin.getId(), AdminLoginFailureReason.ACCOUNT_LOCKED, ip, userAgent);
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(reqDto.getPassword(), admin.getPassword())) {
            adminLoginLogService.recordPasswordFailureAndLock(loginId, admin.getId(), ip, userAgent);
            throw new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
        }

        adminLoginLogService.recordSuccess(loginId, admin.getId(), ip, userAgent);

        String role = admin.getRole().name();
        return AdminLoginRspDto.builder()
                .accessToken(adminJwtTokenProvider.createAccessToken(
                        admin.getId(), admin.getLoginId(), admin.getName(), role))
                .refreshToken(adminJwtTokenProvider.createRefreshToken(admin.getId()))
                .tokenType(TOKEN_TYPE_BEARER)
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .role(role)
                .build();
    }

    @Transactional(readOnly = true)
    public AdminTokenRspDto refresh(AdminRefreshReqDto reqDto) {
        String refreshToken = reqDto.getRefreshToken();
        if (!adminJwtTokenProvider.validateToken(refreshToken) || !adminJwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.ADMIN_TOKEN_INVALID);
        }

        Long adminId = adminJwtTokenProvider.getAdminId(refreshToken);
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        if (!admin.isActive()) {
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_DISABLED);
        }

        String accessToken = adminJwtTokenProvider.createAccessToken(
                admin.getId(), admin.getLoginId(), admin.getName(), admin.getRole().name());

        return AdminTokenRspDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 현재 세션 관리자 정보 조회.
    @Transactional(readOnly = true)
    public AdminSummaryDto getMe(Long adminId) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
        return AdminSummaryDto.from(admin);
    }

    // 로그아웃
    public void logout(Long adminId) {
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return comma > 0 ? xff.substring(0, comma).trim() : xff.trim();
        }
        return request.getRemoteAddr();
    }
}
