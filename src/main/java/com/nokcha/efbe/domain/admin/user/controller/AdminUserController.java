package com.nokcha.efbe.domain.admin.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.admin.user.dto.request.AdminProfileRejectReqDto;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserDetailRspDto;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserSummaryRspDto;
import com.nokcha.efbe.domain.admin.user.service.AdminUserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 어드민 유저 관리 API
@Tag(name = "Admin User", description = "관리자 유저 관리 (목록·상세)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/user")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "유저 목록 조회",
            description = "keyword(닉네임/로그인ID/UUID LIKE), status(ACTIVE/TEMP_SUSPENDED/PERMANENTLY_SUSPENDED) 동적 필터")
    @GetMapping
    public RspTemplate<Page<AdminUserSummaryRspDto>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 15, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "유저 목록을 조회했습니다.",
                adminUserService.getUsers(keyword, status, pageable));
    }

    @Operation(summary = "유저 단건 상세",
            description = "id 기준. 기본정보 + 프로필 + 결제 집계 + 최근 접속 이력 + 작성 글(포스트잇/밸런스댓글)")
    @GetMapping("/{id}")
    public RspTemplate<AdminUserDetailRspDto> getUser(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "유저 상세를 조회했습니다.",
                adminUserService.getUser(id));
    }

    @Operation(summary = "프로필 심사 승인 (반려 → APPROVED 복구)",
            description = "REJECTED 상태였던 프로필을 APPROVED 로 변경." +
                    "심사자/심사 시각은 현재 관리자/현재 시각으로 기록. " +
                    "신규 가입은 자동 APPROVED 라 별도 호출 불필요.")
    @PatchMapping("/{id}/profile/approve")
    public RspTemplate<AdminUserDetailRspDto> approveProfile(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "프로필이 승인되었습니다.",
                adminUserService.approveProfile(id, securityUtil.getCurrentUserId()));
    }

    @Operation(summary = "프로필 심사 반려 (APPROVED → REJECTED)",
            description = "APPROVED 상태의 프로필을 REJECTED 로 전환. 사유는 유저에게 노출됨.")
    @PatchMapping("/{id}/profile/reject")
    public RspTemplate<AdminUserDetailRspDto> rejectProfile(
            @PathVariable Long id,
            @Valid @RequestBody AdminProfileRejectReqDto req
    ) {
        return new RspTemplate<>(HttpStatus.OK, "프로필이 반려되었습니다.",
                adminUserService.rejectProfile(id, req.getReason(), securityUtil.getCurrentUserId()));
    }
}
