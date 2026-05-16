package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.response.UserActivityPostItRspDto;
import com.nokcha.efbe.domain.postIt.dto.response.UserActivityReactedPostItRspDto;
import com.nokcha.efbe.domain.postIt.service.PostItUserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 포스트잇 — 내 활동 RESTful 컨트롤러 (마이페이지)
// 두 탭: 내가 붙인(/my/posts) / 내가 반응한(/my/reactions)
@Tag(name = "PostIt UserActivity", description = "포스트잇 — 내 활동 (마이페이지 — 내가 붙인 / 내가 반응한)")
@RestController
@RequestMapping("/v1/post-it/my")
@RequiredArgsConstructor
public class PostItUserActivityController {

    private final PostItUserActivityService postItUserActivityService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내가 붙인 포스트잇 목록",
            description = "현재 로그인 유저가 작성한 포스트잇을 커서 페이지네이션으로 반환합니다. " +
                    "정렬: post_it.create_time DESC, id DESC. " +
                    "각 카드에 본인 글 표시 정책(익명이면 nickname='익명', age/location=null) 이 적용되며, " +
                    "likeCount + chatCount(post_chat_room 전체 — active/closed 무관) 동시 노출.")
    @GetMapping("/posts")
    public ResponseEntity<RspTemplate<CursorPageResponse<UserActivityPostItRspDto>>> getMyPosts(
            @Parameter(description = "이전 응답의 nextCursor — 첫 페이지는 비워서 호출")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 번에 가져올 카드 수 (기본 20, 최대 50)")
            @RequestParam(required = false) Integer size) {
        Long userId = securityUtil.getCurrentUserId();
        CursorPageResponse<UserActivityPostItRspDto> data =
                postItUserActivityService.getMyPosts(userId, cursor, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내가 붙인 포스트잇 목록 조회 성공", data));
    }

    @Operation(summary = "내가 반응한 포스트잇 목록",
            description = "현재 로그인 유저가 좋아요했거나 채팅(partner)으로 참여한 상대 글 목록을 커서 페이지네이션으로 반환합니다. " +
                    "본인 작성 글은 제외. 정렬: post_it.create_time DESC, id DESC. " +
                    "각 카드는 likedByMe / chattedByMe 플래그와 likeCount / chatCount 를 함께 포함합니다. " +
                    "FE 는 likedByMe / chattedByMe 조합에 따라 좋아요/채팅 카운트 표시를 결정합니다.")
    @GetMapping("/reactions")
    public ResponseEntity<RspTemplate<CursorPageResponse<UserActivityReactedPostItRspDto>>> getMyReactions(
            @Parameter(description = "이전 응답의 nextCursor — 첫 페이지는 비워서 호출")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "한 번에 가져올 카드 수 (기본 20, 최대 50)")
            @RequestParam(required = false) Integer size) {
        Long userId = securityUtil.getCurrentUserId();
        CursorPageResponse<UserActivityReactedPostItRspDto> data =
                postItUserActivityService.getMyReactions(userId, cursor, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내가 반응한 포스트잇 목록 조회 성공", data));
    }
}
