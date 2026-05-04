package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.postIt.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포스트잇 좋아요 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/post-it/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요
    @PostMapping
    public ResponseEntity<RspTemplate<Void>> createLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        postLikeService.createLike(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "좋아요 성공"));
    }

    // 좋아요 취소
    @DeleteMapping
    public ResponseEntity<RspTemplate<Void>> deleteLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        postLikeService.deleteLike(postId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "좋아요 취소 성공"));
    }
}
