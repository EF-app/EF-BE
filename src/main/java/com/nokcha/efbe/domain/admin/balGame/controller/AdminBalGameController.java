package com.nokcha.efbe.domain.admin.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalCommentRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalGameDetailRspDto;
import com.nokcha.efbe.domain.admin.balGame.dto.response.AdminBalGameSummaryRspDto;
import com.nokcha.efbe.domain.admin.balGame.service.AdminBalGameService;
import com.nokcha.efbe.domain.admin.balGame.dto.request.AdminBalGameReqDto;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin BalGame", description = "관리자 밸런스 게임 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/bal-game")
public class AdminBalGameController {

    private final AdminBalGameService adminBalGameService;

    @Operation(summary = "밸런스 게임 목록 조회", description = "status / categoryCode 옵션. 생략 시 전체 상태/카테고리 노출")
    @GetMapping
    public RspTemplate<Page<AdminBalGameSummaryRspDto>> getGames(
            @RequestParam(required = false) BalGameStatus status,
            @RequestParam(required = false) BalCategoryCode categoryCode,
            @PageableDefault(size = 12) Pageable pageable) {
        return new RspTemplate<>(HttpStatus.OK, "밸런스 게임 목록을 조회했습니다.", adminBalGameService.getGames(status, categoryCode, pageable));
    }

    @Operation(summary = "밸런스 게임 단건 상세", description = "status 모든 상태 노출")
    @GetMapping("/{gameId}")
    public RspTemplate<AdminBalGameDetailRspDto> getGame(@PathVariable Long gameId) {
        return new RspTemplate<>(HttpStatus.OK, "밸런스 게임 상세를 조회했습니다.", adminBalGameService.getGame(gameId));
    }

    @Operation(summary = "관리자 밸런스 게임 등록",
            description = "관리자 등록 or BAL-APPLY prefill 로 신규 등록. " +
                    "status 생략 시 DRAFT. SCHEDULED 면 scheduledAt 필수(현재 이후, 10분 단위). " +
                    "PUBLISHED 면 즉시 게시. ARCHIVED 는 종료. " +
                    "applyId 가 있으면 해당 BalApply 가 PENDING → APPROVED 로 처리되고 신청자가 applicant 로 연결됨.")
    @PostMapping
    public RspTemplate<AdminBalGameDetailRspDto> createGame(
            @Valid @RequestBody AdminBalGameReqDto req) {
        return new RspTemplate<>(HttpStatus.CREATED, "밸런스 게임이 등록되었습니다.", adminBalGameService.createGame(req));
    }

    @Operation(summary = "밸런스 게임 수정", description = "상태 / 일정 / 내용 변경")
    @PatchMapping("/{gameId}")
    public RspTemplate<AdminBalGameDetailRspDto> updateGame(
            @PathVariable Long gameId,
            @Valid @RequestBody AdminBalGameReqDto req) {
        return new RspTemplate<>(HttpStatus.OK, "밸런스 게임이 수정되었습니다.", adminBalGameService.updateGame(gameId, req));
    }

    @Operation(summary = "밸런스 게임 댓글 목록", description = "숨김/삭제 포함 밸런스 게임 모든 댓글 조회")
    @GetMapping("/{gameId}/comments")
    public RspTemplate<Page<AdminBalCommentRspDto>> getComments(
            @PathVariable Long gameId,
            @PageableDefault(size = 50, sort = "createTime", direction = Sort.Direction.ASC) Pageable pageable) {
        return new RspTemplate<>(HttpStatus.OK, "밸런스 게임 댓글을 조회했습니다.", adminBalGameService.getComments(gameId, pageable));
    }
}
