package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.request.CommentCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.CommentRspDto;
import com.nokcha.efbe.domain.balGame.service.BalCommentLikeService;
import com.nokcha.efbe.domain.balGame.service.BalGameCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "BalGame Comment", description = "밸런스 게임 댓글/대댓글/좋아요")
@RestController
@RequestMapping("/v1/bal-game/{gameId}/comments")
@RequiredArgsConstructor
public class BalCommentController {

    private final BalGameCommentService balGameCommentService;
    private final BalCommentLikeService balCommentLikeService;
    private final SecurityUtil securityUtil;

    // 댓글/대댓글 작성 — id 기반 (parentId 가 있으면 대댓글)
    @Operation(summary = "댓글/대댓글 작성", description = "parentId 가 있으면 대댓글로 등록됨")
    @PostMapping
    public RspTemplate<CommentRspDto> createComment(
            @PathVariable Long gameId,
            @Valid @RequestBody CommentCreateReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        CommentRspDto data = balGameCommentService.createComment(gameId, userId, req);
        return new RspTemplate<>(HttpStatus.CREATED, "댓글 작성 성공", data);
    }

    // 댓글 트리 조회 (맨 아래가 최신)
    @Operation(summary = "댓글 트리 조회", description = "오래된 순으로 정렬 — 맨 아래가 최신")
    @GetMapping
    public RspTemplate<List<CommentRspDto>> getComments(@PathVariable Long gameId) {
        Long userId = securityUtil.getCurrentUserId();
        List<CommentRspDto> data = balGameCommentService.getComments(gameId, userId);
        return new RspTemplate<>(HttpStatus.OK, "댓글 목록 조회 성공", data);
    }

    // 메인홈 카드용 — 해당 게임의 최신 top-level 댓글 N개 (기본 3개, 대댓글 제외)
    @Operation(summary = "홈 카드용 최신 댓글 조회", description = "해당 게임의 최신 top-level 댓글 N개 (기본 3개, 대댓글 제외)")
    @GetMapping("/recent")
    public RspTemplate<List<CommentRspDto>> getRecentComments(
            @PathVariable Long gameId,
            @RequestParam(required = false) Integer size) {
        Long viewerId = securityUtil.getCurrentUserIdOrSystem();
        List<CommentRspDto> data = balGameCommentService.getRecentComments(gameId, viewerId, size);
        return new RspTemplate<>(HttpStatus.OK, "최신 댓글 조회 성공", data);
    }

    // 본인 댓글 삭제 — id 기반
    @Operation(summary = "본인 댓글 삭제")
    @DeleteMapping("/{commentId}")
    public RspTemplate<Void> deleteComment(@PathVariable Long gameId,
                                                           @PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        balGameCommentService.deleteComment(gameId, commentId, userId);
        return new RspTemplate<>(HttpStatus.OK, "댓글 삭제 성공");
    }

    // 댓글 좋아요
    @Operation(summary = "댓글 좋아요")
    @PostMapping("/{commentId}/likes")
    public RspTemplate<Void> createLike(@PathVariable Long gameId, @PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        balCommentLikeService.createLike(commentId, userId);
        return new RspTemplate<>(HttpStatus.CREATED, "좋아요 성공");
    }

    // 댓글 좋아요 취소
    @Operation(summary = "댓글 좋아요 취소")
    @DeleteMapping("/{commentId}/likes")
    public RspTemplate<Void> deleteLike(@PathVariable Long gameId, @PathVariable Long commentId) {
        Long userId = securityUtil.getCurrentUserId();
        balCommentLikeService.deleteLike(commentId, userId);
        return new RspTemplate<>(HttpStatus.OK, "좋아요 취소 성공");
    }
}
