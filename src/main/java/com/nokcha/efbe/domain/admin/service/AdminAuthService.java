package com.nokcha.efbe.domain.admin.service;

import com.nokcha.efbe.common.auth.jwt.JwtTokenProvider;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.entity.Admin;
import com.nokcha.efbe.domain.admin.repository.AdminRepository;
import com.nokcha.efbe.domain.user.dto.request.LoginReqDto;
import com.nokcha.efbe.domain.user.dto.request.RefreshTokenReqDto;
import com.nokcha.efbe.domain.user.dto.response.LoginRspDto;
import com.nokcha.efbe.domain.user.dto.response.TokenRefreshRspDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginRspDto login(LoginReqDto reqDto) {
        Admin admin = adminRepository.findByLoginId(reqDto.getLoginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(reqDto.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        return LoginRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .refreshToken(jwtTokenProvider.createRefreshToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .loginId(admin.getLoginId())
                .build();
    }

    public TokenRefreshRspDto refreshAccessToken(RefreshTokenReqDto reqDto) {
        jwtTokenProvider.validateRefreshToken(reqDto.getRefreshToken());

        if (!ADMIN_ROLE.equals(jwtTokenProvider.getRole(reqDto.getRefreshToken()))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String loginId = jwtTokenProvider.getLoginId(reqDto.getRefreshToken());
        Admin admin = adminRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_USER));

        return TokenRefreshRspDto.builder()
                .accessToken(jwtTokenProvider.createAccessToken(admin.getId(), admin.getLoginId(), ADMIN_ROLE))
                .loginId(admin.getLoginId())
                .build();
    }
}
