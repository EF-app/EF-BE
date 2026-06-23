package com.nokcha.efbe.domain.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.suspension.dto.response.UserSuspensionRspDto;
import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.user.dto.response.AccountMaskedRspDto;
import com.nokcha.efbe.domain.user.dto.response.AccountRevealRspDto;
import com.nokcha.efbe.domain.user.dto.request.*;
import com.nokcha.efbe.domain.user.dto.response.UserSummaryRspDto;
import com.nokcha.efbe.domain.user.service.UserInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Info", description = "회원 정보 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/me")
public class UserInfoController {

    private final UserInfoService userInfoService;
    private final SuspensionService suspensionService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내 정보 요약 조회", description = "로그인한 회원의 닉네임 / 지역 / 나이를 반환합니다. 포스트잇 글쓰기 화면 / My 탭 등 공용으로 사용.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/summary")
    public RspTemplate<UserSummaryRspDto> getMySummary() {
        return new RspTemplate<>(HttpStatus.OK, "내 정보 조회 성공", userInfoService.getMySummary());
    }

    @Operation(summary = "내 활성 제재 조회",
            description = "차단 화면 진입 시 호출. 활성 제재 없으면 active=false, 있으면 type/reason/endsAt 반환. " +
                    "로그인 응답(LoginRspDto.suspension) 으로도 inline 전달됨 — 신규 제재가 부과된 경우나 화면 재진입 시 재확인용.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/suspensions")
    public RspTemplate<UserSuspensionRspDto> getMySuspension() {
        Long userId = securityUtil.getCurrentUserId();
        UserSuspensionRspDto data = suspensionService.findActiveBlockingSuspension(userId)
                .map(UserSuspensionRspDto::from)
                .orElseGet(UserSuspensionRspDto::inactive);
        return new RspTemplate<>(HttpStatus.OK, "활성 제재 조회 성공", data);
    }

    @Operation(summary = "FCM 토큰 등록/갱신", description = "클라이언트가 Firebase SDK에서 발급받은 FCM registration token을 저장합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/fcm-token")
    public RspTemplate<Void> updateFcmToken(@Valid @RequestBody FcmTokenReqDto reqDto) {
        userInfoService.updateFcmToken(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "FCM 토큰이 저장되었습니다.");
    }

    @Operation(summary = "FCM 토큰 삭제", description = "로그아웃, 알림 비활성화, 토큰 폐기 시 저장된 FCM token을 삭제합니다.")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/fcm-token")
    public RspTemplate<Void> deleteFcmToken() {
        userInfoService.deleteFcmToken();
        return new RspTemplate<>(HttpStatus.OK, "FCM 토큰이 삭제되었습니다.");
    }

    @Operation(summary = "보안코드 설정", description = "로그인한 회원의 보안코드를 설정합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/scode")
    public RspTemplate<Void> createScode(@Valid @RequestBody UserScodeReqDto reqDto) {
        userInfoService.createScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드 설정이 완료되었습니다.");
    }

    @Operation(summary = "보안코드 변경", description = "기존 보안코드 검증 후 새 보안코드로 변경합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode")
    public RspTemplate<Void> changeScode(@Valid @RequestBody ScodeChangeReqDto reqDto) {
        userInfoService.changeScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 변경되었습니다.");
    }

    @Operation(summary = "보안코드 검증", description = "변경 화면에서 기존 보안코드 입력 직후 즉시 검증합니다. 일치하면 200, 불일치 시 WRONG_SCODE 401.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/scode/verify")
    public RspTemplate<Void> verifyScode(@Valid @RequestBody ScodeVerifyReqDto reqDto) {
        userInfoService.verifyScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 일치합니다.");
    }

    @Operation(summary = "보안코드 초기화(리셋)", description = "기존 보안코드를 잊은 경우, 비밀번호 재인증을 통과한 사용자에 한해 새 보안코드로 강제 재설정합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode/reset")
    public RspTemplate<Void> resetScode(@Valid @RequestBody ScodeResetReqDto reqDto) {
        userInfoService.resetScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드가 초기화되었습니다.");
    }

    @Operation(summary = "계정 정보 마스킹 조회", description = "계정관리 화면 진입 시 마스킹된 로그인 아이디와 이메일을 반환합니다.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/account")
    public RspTemplate<AccountMaskedRspDto> getAccount() {
        return new RspTemplate<>(HttpStatus.OK, "계정 정보 조회 성공", userInfoService.getMaskedAccount());
    }

    @Operation(summary = "계정 정보 전체 조회", description = "비밀번호 재인증 후 전체 로그인 아이디와 이메일을 반환합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/account/reveal")
    public RspTemplate<AccountRevealRspDto> revealAccount(@Valid @RequestBody AccountRevealReqDto reqDto) {
        return new RspTemplate<>(HttpStatus.OK, "계정 정보 전체 조회 성공", userInfoService.revealAccount(reqDto));
    }

    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호 검증 후 새 비밀번호로 변경합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/password")
    public RspTemplate<Void> changePassword(@Valid @RequestBody PasswordChangeReqDto reqDto) {
        userInfoService.changePassword(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 회원을 탈퇴 처리하고 탈퇴 사유를 기록합니다.")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/withdrawal")
    public RspTemplate<Void> withdraw(@Valid @RequestBody UserWithdrawalReqDto reqDto, HttpServletRequest request) {
        userInfoService.withdraw(reqDto, request);
        return new RspTemplate<>(HttpStatus.OK, "회원 탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "회원 탈퇴 취소", description = "대기 중인 탈퇴 요청을 사용자가 직접 취소합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/withdrawal/cancel")
    public RspTemplate<Void> cancelWithdrawal() {
        userInfoService.cancelWithdrawal();
        return new RspTemplate<>(HttpStatus.OK, "회원 탈퇴가 취소되었습니다.");
    }
}
