package com.nokcha.efbe.domain.admin.auth.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminInfoRspDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminLoginRspDto;
import com.nokcha.efbe.domain.admin.auth.service.AdminAuthService;
import com.nokcha.efbe.domain.user.dto.request.LoginReqDto;
import com.nokcha.efbe.domain.user.dto.request.RefreshTokenReqDto;
import com.nokcha.efbe.domain.user.dto.response.TokenRefreshRspDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Auth", description = "관리자 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인하고 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public RspTemplate<AdminLoginRspDto> login(@Valid @RequestBody LoginReqDto reqDto, HttpServletRequest request) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 로그인이 완료되었습니다.", adminAuthService.login(reqDto, request));
    }

    @Operation(summary = "관리자 액세스 토큰 재발급", description = "관리자 리프레시 토큰으로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/token/refresh")
    public RspTemplate<TokenRefreshRspDto> refreshAccessToken(@Valid @RequestBody RefreshTokenReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 액세스 토큰 재발급이 완료되었습니다.", adminAuthService.refreshAccessToken(reqDto));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 관리자 정보를 조회합니다.")
    @GetMapping("/me")
    public RspTemplate<AdminInfoRspDto> getMe() {
        return new RspTemplate<>(HttpStatus.OK, "관리자 정보를 조회했습니다.", adminAuthService.getAdmin(securityUtil.getCurrentUserId()));
    }
}
