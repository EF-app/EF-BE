package com.nokcha.efbe.domain.admin.block.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.block.dto.response.AdminBlockRspDto;
import com.nokcha.efbe.domain.admin.block.service.AdminBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 어드민 차단 내역 API
@Tag(name = "Admin Block", description = "관리자 차단 내역 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/block")
public class AdminBlockController {

    private final AdminBlockService adminBlockService;

    @Operation(summary = "차단 내역 목록 조회",
            description = "keyword(차단자/피차단자 닉네임·UUID LIKE) 동적 필터. 최신순.")
    @GetMapping
    public RspTemplate<Page<AdminBlockRspDto>> getBlocks(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "차단 내역을 조회했습니다.",
                adminBlockService.getBlocks(keyword, pageable));
    }
}
