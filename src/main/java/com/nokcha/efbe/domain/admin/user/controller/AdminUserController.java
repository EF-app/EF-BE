package com.nokcha.efbe.domain.admin.user.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserDetailRspDto;
import com.nokcha.efbe.domain.admin.user.dto.response.AdminUserSummaryRspDto;
import com.nokcha.efbe.domain.admin.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            description = "id 기준. 기본정보 + 프로필 + 결제 집계 + 최근 접속 이력. " +
                    "작성 글(포스트잇/밸런스댓글)은 별도 엔드포인트에서 조회.")
    @GetMapping("/{id}")
    public RspTemplate<AdminUserDetailRspDto> getUser(@PathVariable Long id) {
        return new RspTemplate<>(HttpStatus.OK, "유저 상세를 조회했습니다.",
                adminUserService.getUser(id));
    }
}
