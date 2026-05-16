package com.nokcha.efbe.domain.admin.auth.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.auth.dto.request.AdminLoginReqDto;
import com.nokcha.efbe.domain.admin.auth.dto.request.AdminRefreshReqDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminLoginRspDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminSummaryDto;
import com.nokcha.efbe.domain.admin.auth.dto.response.AdminTokenRspDto;
import com.nokcha.efbe.domain.admin.auth.service.AdminAuthService;
import com.nokcha.efbe.common.util.AdminSecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth", description = "관리자 로그인·로그아웃·토큰 갱신 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminSecurityUtil adminSecurityUtil;

    @Operation(summary = "관리자 로그인", description = "아이디와 비밀번호를 검증하고 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/login")
    public RspTemplate<AdminLoginRspDto> login(@Valid @RequestBody AdminLoginReqDto reqDto,
                                               HttpServletRequest request) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 로그인이 완료되었습니다.",
                adminAuthService.login(reqDto, request));
    }

    @Operation(summary = "관리자 토큰 갱신", description = "Refresh Token 으로 Access Token 을 재발급합니다.")
    @PostMapping("/token/refresh")
    public RspTemplate<AdminTokenRspDto> refresh(@Valid @RequestBody AdminRefreshReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 토큰이 갱신되었습니다.",
                adminAuthService.refresh(reqDto));
    }

    @Operation(summary = "관리자 로그아웃", description = "현재 세션을 종료합니다. (클라이언트 토큰 폐기)")
    @PostMapping("/logout")
    public RspTemplate<Void> logout() {
        adminAuthService.logout(adminSecurityUtil.getCurrentAdminId());
        return new RspTemplate<>(HttpStatus.OK, "관리자 로그아웃이 완료되었습니다.");
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 관리자 정보를 조회합니다.")
    @GetMapping("/me")
    public RspTemplate<AdminSummaryDto> getMe() {
        return new RspTemplate<>(HttpStatus.OK, "관리자 정보를 조회했습니다.",
                adminAuthService.getMe(adminSecurityUtil.getCurrentAdminId()));
    }
}
