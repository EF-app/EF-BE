package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.response.PostLikeRspDto;
import com.nokcha.efbe.domain.postIt.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 포스트잇 좋아요 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/post-it/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요 - 갱신된 likeCount/likedByMe 반환
    @PostMapping
    public ResponseEntity<RspTemplate<PostLikeRspDto>> createLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostLikeRspDto data = postLikeService.createLike(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "좋아요 성공", data));
    }

    // 좋아요 취소 - 갱신된 likeCount/likedByMe 반환
    @DeleteMapping
    public ResponseEntity<RspTemplate<PostLikeRspDto>> deleteLike(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostLikeRspDto data = postLikeService.deleteLike(postId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "좋아요 취소 성공", data));
    }
}
