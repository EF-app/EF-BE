package com.nokcha.efbe.domain.user.account.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.user.account.dto.request.AccountRevealReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.PasswordChangeReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeChangeReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeResetReqDto;
import com.nokcha.efbe.domain.user.account.dto.request.ScodeVerifyReqDto;
import com.nokcha.efbe.domain.user.account.dto.response.AccountMaskedRspDto;
import com.nokcha.efbe.domain.user.account.dto.response.AccountRevealRspDto;
import com.nokcha.efbe.domain.user.account.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Account", description = "계정 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me")
public class UserAccountController {

    private final UserAccountService userAccountService;

    @Operation(summary = "계정 정보 마스킹 조회",
            description = "계정관리 화면 진입 시 마스킹된 로그인 아이디와 이메일을 반환합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/account")
    public RspTemplate<AccountMaskedRspDto> getAccount() {
        return new RspTemplate<>(HttpStatus.OK, "계정 정보 조회 성공", userAccountService.getMaskedAccount());
    }

    @Operation(summary = "계정 정보 전체 조회",
            description = "비밀번호 재인증 후 전체 로그인 아이디와 이메일을 반환합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/account/reveal")
    public RspTemplate<AccountRevealRspDto> revealAccount(@Valid @RequestBody AccountRevealReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "계정 정보 전체 조회 성공", userAccountService.revealAccount(reqDto));
    }

    @Operation(summary = "비밀번호 변경",
            description = "기존 비밀번호 검증 후 새 비밀번호로 변경합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/password")
    public RspTemplate<Void> changePassword(@Valid @RequestBody PasswordChangeReqDto reqDto) {
        userAccountService.changePassword(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "보안코드 검증",
            description = "변경 화면에서 기존 보안코드 입력 직후 즉시 검증합니다. 일치하면 200, 불일치 시 WRONG_SCODE 401.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/scode/verify")
    public RspTemplate<Void> verifyScode(@Valid @RequestBody ScodeVerifyReqDto reqDto) {
        userAccountService.verifyScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 일치합니다.");
    }

    @Operation(summary = "보안코드 변경",
            description = "기존 보안코드 검증 후 새 보안코드로 변경합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode/change")
    public RspTemplate<Void> changeScode(@Valid @RequestBody ScodeChangeReqDto reqDto) {
        userAccountService.changeScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 변경되었습니다.");
    }

    @Operation(summary = "보안코드 초기화(리셋)",
            description = "기존 보안코드를 잊은 경우, 비밀번호 재인증을 통과한 사용자에 한해 새 보안코드로 강제 재설정합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode/reset")
    public RspTemplate<Void> resetScode(@Valid @RequestBody ScodeResetReqDto reqDto) {
        userAccountService.resetScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 초기화되었습니다.");
    }
}
