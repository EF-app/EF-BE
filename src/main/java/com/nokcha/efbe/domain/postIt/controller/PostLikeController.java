package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.response.PostLikeRspDto;
import com.nokcha.efbe.domain.postIt.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PostIt Like", description = "포스트잇 좋아요 API")
@RestController
@RequestMapping("/v1/post-it/{uuid}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "포스트잇 좋아요", description = "uuid 식별자로 좋아요. 갱신된 likeCount/likedByMe 반환.")
    @PostMapping
    public ResponseEntity<RspTemplate<PostLikeRspDto>> createLike(@PathVariable String uuid) {
        Long userId = securityUtil.getCurrentUserId();
        PostLikeRspDto data = postLikeService.createLike(uuid, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "좋아요 성공", data));
    }

    @Operation(summary = "포스트잇 좋아요 취소", description = "본인이 누른 좋아요를 취소. 갱신된 likeCount/likedByMe 반환.")
    @DeleteMapping
    public ResponseEntity<RspTemplate<PostLikeRspDto>> deleteLike(@PathVariable String uuid) {
        Long userId = securityUtil.getCurrentUserId();
        PostLikeRspDto data = postLikeService.deleteLike(uuid, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "좋아요 취소 성공", data));
    }
}
