package com.nokcha.efbe.domain.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.user.dto.request.UserScodeReqDto;
import com.nokcha.efbe.domain.user.dto.request.UserWithdrawalReqDto;
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

    @Operation(summary = "내 정보 요약 조회", description = "로그인한 회원의 닉네임 / 지역 / 나이를 반환합니다. 포스트잇 글쓰기 화면 / My 탭 등 공용으로 사용.")
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/summary")
    public RspTemplate<UserSummaryRspDto> getMySummary() {
        return new RspTemplate<>(HttpStatus.OK, "내 정보 조회 성공", userInfoService.getMySummary());
    }

    @Operation(summary = "보안코드 설정/수정", description = "로그인한 회원의 보안코드를 설정하거나 수정합니다.")
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/scode")
    public RspTemplate<Void> updateScode(@Valid @RequestBody UserScodeReqDto reqDto) {
        userInfoService.updateScode(reqDto);
        return new RspTemplate<>(HttpStatus.OK, "보안코드 설정이 완료되었습니다.");
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
