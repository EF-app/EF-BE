package com.nokcha.efbe.domain.admin.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.postIt.dto.request.AdminPostItHideReqDto;
import com.nokcha.efbe.domain.admin.postIt.dto.response.AdminPostItRspDto;
import com.nokcha.efbe.domain.admin.postIt.service.AdminPostItService;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin PostIt", description = "관리자 포스트잇 (목록·상세·숨김/숨김 해제)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/post-its")
public class AdminPostItController {

    private final AdminPostItService adminPostItService;

    @Operation(summary = "포스트잇 목록 조회", description = "keyword(닉네임/본문 LIKE), categoryCode, isHidden, isDeleted, userId 동적 필터")
    @GetMapping
    public RspTemplate<Page<AdminPostItRspDto>> getPostIts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostCategory categoryCode,
            @RequestParam(required = false) Boolean isHidden,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new RspTemplate<>(HttpStatus.OK, "포스트잇 목록을 조회했습니다.", adminPostItService.getPostIts(keyword, categoryCode, isHidden, isDeleted, userId, pageable));
    }

    @Operation(summary = "포스트잇 단건 상세", description = "id 기준 단건. 모든 상태(숨김/삭제 포함) 노출. 본문은 치환 없이 원본.")
    @GetMapping("/{postId}")
    public RspTemplate<AdminPostItRspDto> getPostIt(@PathVariable Long postId) {
        return new RspTemplate<>(HttpStatus.OK, "포스트잇 상세를 조회했습니다.", adminPostItService.getPostIt(postId));
    }

    @Operation(summary = "포스트잇 숨김 처리", description = "is_hidden = true. 이미 삭제된 글은 거부.")
    @PostMapping("/{postId}/hide")
    public RspTemplate<AdminPostItRspDto> hide(@PathVariable Long postId, @RequestBody(required = false) AdminPostItHideReqDto req) {
        return new RspTemplate<>(HttpStatus.OK, "포스트잇이 숨김 처리되었습니다.", adminPostItService.hide(postId, req));
    }

    @Operation(summary = "포스트잇 숨김 해제", description = "is_hidden = false + report_count = 0 리셋. is_hidden=true 인 글만 가능.")
    @PostMapping("/{postId}/restore")
    public RspTemplate<AdminPostItRspDto> restore(@PathVariable Long postId) {
        return new RspTemplate<>(HttpStatus.OK, "포스트잇 숨김이 해제되었습니다.", adminPostItService.restore(postId));
    }
}
