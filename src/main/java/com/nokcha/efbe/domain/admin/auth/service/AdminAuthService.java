package com.nokcha.efbe.domain.admin.auth.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.util.LoginUtil;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminInfoRspDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminLoginRspDto;
import com.nokcha.efbe.domain.admin.log.service.AdminLoginLogService;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.admin.log.entity.AdminLoginFailureReason;
import com.nokcha.efbe.domain.admin.auth.repository.AdminAccountRepository;
import com.nokcha.efbe.domain.user.dto.request.LoginReqDto;
import com.nokcha.efbe.domain.user.dto.request.RefreshTokenReqDto;
import com.nokcha.efbe.domain.user.dto.response.TokenRefreshRspDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String USER_AGANT =  "User-Agent";

    private final AdminAccountRepository adminAccountRepository;
    private final AdminLoginLogService adminLoginLogService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginUtil loginUtil;

    public AdminLoginRspDto login(LoginReqDto reqDto, HttpServletRequest request) {
        String loginId = reqDto.getLoginId();
        String ip = loginUtil.resolveClientIp(request);
        LocalDateTime now = LocalDateTime.now();

        AdminAccount admin = adminAccountRepository.findByLoginId(loginId)
                .orElseThrow(() -> {
                    adminLoginLogService.recordFailure(loginId, null, AdminLoginFailureReason.INVALID_ID, ip, USER_AGANT);
                    return new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
                });

        if (!admin.isActive()) {
            adminLoginLogService.recordFailure(loginId, admin.getId(), AdminLoginFailureReason.ACCOUNT_INACTIVE, ip, USER_AGANT);
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_DISABLED);
        }

        if (admin.isLocked(now)) {
            adminLoginLogService.recordFailure(loginId, admin.getId(), AdminLoginFailureReason.ACCOUNT_LOCKED, ip, USER_AGANT);
            throw new BusinessException(ErrorCode.ADMIN_ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(reqDto.getPassword(), admin.getPassword())) {
            adminLoginLogService.recordPasswordFailureAndLock(loginId, admin.getId(), ip, USER_AGANT);
            throw new BusinessException(ErrorCode.ADMIN_LOGIN_FAILED);
        }

        adminLoginLogService.recordSuccess(loginId, admin.getId(), ip, USER_AGANT);

        return AdminLoginRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .refreshToken(jwtTokenProvider.createRefreshToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .build();
    }

    public TokenRefreshRspDto refreshAccessToken(RefreshTokenReqDto reqDto) {
        jwtTokenProvider.validateRefreshToken(reqDto.getRefreshToken());

        if (!ADMIN_ROLE.equals(jwtTokenProvider.getRole(reqDto.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String loginId = jwtTokenProvider.getLoginId(reqDto.getRefreshToken());
        AdminAccount admin = adminAccountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        return TokenRefreshRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .loginId(admin.getLoginId())
                .build();
    }

    // 관리자 정보 조회
    @Transactional(readOnly = true)
    public AdminInfoRspDto getAdmin(Long adminId) {
        AdminAccount admin = adminAccountRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));

        return AdminInfoRspDto.from(admin);
    }
}
