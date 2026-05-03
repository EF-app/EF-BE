package com.nokcha.efbe.domain.balGame.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.balGame.dto.request.CommentCreateReqDto;
import com.nokcha.efbe.domain.balGame.dto.response.CommentRspDto;
import com.nokcha.efbe.domain.balGame.service.BalCommentLikeService;
import com.nokcha.efbe.domain.balGame.service.BalGameCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 밸런스 게임 댓글/대댓글/좋아요 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/bal-game/{gameId}/comments")
@RequiredArgsConstructor
public class BalCommentController {

    private final BalGameCommentService balGameCommentService;
    private final BalCommentLikeService balCommentLikeService;

    // 댓글/대댓글 작성 (parentId 가 있으면 대댓글)
    @PostMapping
    public ResponseEntity<RspTemplate<CommentRspDto>> createComment(
            @PathVariable Long gameId,
            @Valid @RequestBody CommentCreateReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        CommentRspDto data = balGameCommentService.createComment(gameId, userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "댓글 작성 성공", data));
    }

    // 댓글 트리 조회 (오래된 순 - 맨 아래가 최신)
    @GetMapping
    public ResponseEntity<RspTemplate<List<CommentRspDto>>> getComments(@PathVariable Long gameId) {
        Long userId = SecurityUtil.getCurrentUserId();
        List<CommentRspDto> data = balGameCommentService.getComments(gameId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "댓글 목록 조회 성공", data));
    }

    // 메인홈 카드용 — 해당 게임의 최신 top-level 댓글 N개 (기본 3개, 대댓글 제외)
    @GetMapping("/recent")
    public ResponseEntity<RspTemplate<List<CommentRspDto>>> getRecentComments(
            @PathVariable Long gameId,
            @RequestParam(required = false) Integer size) {
        Long viewerId = SecurityUtil.getCurrentUserIdOrSystem();
        List<CommentRspDto> data = balGameCommentService.getRecentComments(gameId, viewerId, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "최신 댓글 조회 성공", data));
    }

    // 본인 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<RspTemplate<Void>> deleteComment(@PathVariable Long gameId,
                                                           @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        balGameCommentService.deleteComment(gameId, commentId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "댓글 삭제 성공"));
    }

    // 댓글 좋아요
    @PostMapping("/{commentId}/likes")
    public ResponseEntity<RspTemplate<Void>> createLike(@PathVariable Long gameId,
                                                        @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        balCommentLikeService.createLike(commentId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "좋아요 성공"));
    }

    // 댓글 좋아요 취소
    @DeleteMapping("/{commentId}/likes")
    public ResponseEntity<RspTemplate<Void>> deleteLike(@PathVariable Long gameId,
                                                        @PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        balCommentLikeService.deleteLike(commentId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "좋아요 취소 성공"));
    }
}
