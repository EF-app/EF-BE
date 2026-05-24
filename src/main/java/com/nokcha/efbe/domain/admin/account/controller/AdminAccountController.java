package com.nokcha.efbe.domain.admin.account.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminAccountCreateReqDto;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminAccountUpdateReqDto;
import com.nokcha.efbe.domain.admin.account.dto.request.AdminPasswordResetReqDto;
import com.nokcha.efbe.domain.admin.account.dto.response.AdminAccountRspDto;
import com.nokcha.efbe.domain.admin.account.service.AdminAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 시스템 > 관리자계정 화면 — CRUD + 비밀번호 변경
@Tag(name = "Admin Account", description = "관리자 계정 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/account")
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @Operation(summary = "관리자 계정 목록",
            description = "keyword(name/loginId/email LIKE) / isActive 동적 필터. 최신순.")
    @GetMapping
    public RspTemplate<Page<AdminAccountRspDto>> getAdmins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 15, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 계정 목록을 조회했습니다.",
                adminAccountService.getAdmins(keyword, isActive, pageable));
    }

    @Operation(summary = "관리자 계정 단건 상세", description = "마지막 성공 로그인 시각/IP 포함 (admin_login_log).")
    @GetMapping("/{id}")
    public RspTemplate<AdminAccountRspDto> getAdmin(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 계정을 조회했습니다.",
                adminAccountService.getAdmin(id));
    }

    @Operation(summary = "관리자 계정 생성", description = "loginId 중복 시 409. 비밀번호는 BE 에서 bcrypt 해시.")
    @PostMapping
    public RspTemplate<AdminAccountRspDto> createAdmin(@Valid @RequestBody AdminAccountCreateReqDto req) {
        return new RspTemplate<>(HttpStatus.CREATED, "관리자 계정이 생성되었습니다.",
                adminAccountService.createAdmin(req));
    }

    @Operation(summary = "관리자 계정 수정",
            description = "email/isActive 갱신. null 필드는 변경 안 함. loginId/name/비밀번호는 변경 불가 (비밀번호는 별도 API).")
    @PatchMapping("/{id}")
    public RspTemplate<AdminAccountRspDto> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminAccountUpdateReqDto req
    ) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 계정이 수정되었습니다.",
                adminAccountService.updateAdmin(id, req));
    }

    @Operation(summary = "관리자 비밀번호 강제 변경",
            description = "현재 비밀번호 확인 없이 즉시 교체. 다른 관리자 비번 리셋용 (운영 사고 대응).")
    @PatchMapping("/{id}/password")
    public RspTemplate<Void> forceChangePassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminPasswordResetReqDto req
    ) {
        adminAccountService.forceChangePassword(id, req);
        return new RspTemplate<>(HttpStatus.OK, "비밀번호가 강제 변경되었습니다.");
    }

    @Operation(summary = "관리자 계정 잠금 해제",
            description = "비밀번호 실패 누적으로 잠긴 계정(lockedUntil) 을 즉시 해제. 잠금 상태가 아니어도 호출 가능 (idempotent).")
    @PatchMapping("/{id}/unlock")
    public RspTemplate<AdminAccountRspDto> unlock(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "관리자 계정 잠금이 해제되었습니다.",
                adminAccountService.unlock(id));
    }
}
