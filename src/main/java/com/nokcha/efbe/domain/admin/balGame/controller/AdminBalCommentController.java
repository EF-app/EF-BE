package com.nokcha.efbe.domain.admin.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminUserBalCommentRspDto;
import com.nokcha.efbe.domain.admin.balGame.service.AdminUserBalCommentService;
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

// 어드민 밸런스 댓글 API — 유저 기준 조회 (유저 상세 "작성한 글" 탭)
@Tag(name = "Admin BalComment", description = "관리자 밸런스 게임 댓글 (유저별 조회)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/bal-comment")
public class AdminBalCommentController {

    private final AdminUserBalCommentService adminUserBalCommentService;

    @Operation(summary = "유저가 작성한 밸런스 댓글 목록",
            description = "userId 의 모든 밸런스 게임 댓글 (숨김/삭제 포함, 최신순)")
    @GetMapping
    public RspTemplate<Page<AdminUserBalCommentRspDto>> getUserComments(
            @RequestParam Long userId,
            @PageableDefault(size = 50, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "유저의 밸런스 댓글을 조회했습니다.",
                adminUserBalCommentService.getUserComments(userId, pageable));
    }
}
