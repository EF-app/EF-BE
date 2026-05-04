package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.request.PostCreateReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostItRspDto;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.service.PostItService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포스트잇 RESTful 컨트롤러
@RestController
@RequestMapping("/v1/post-it")
@RequiredArgsConstructor
public class PostItController {

    private final PostItService postItService;

    // 포스트잇 작성
    @PostMapping
    public ResponseEntity<RspTemplate<PostItRspDto>> createPostIt(@Valid @RequestBody PostCreateReqDto req) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostItRspDto data = postItService.createPostIt(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "포스트잇 작성 성공", data));
    }

    // 피드 조회 (커서 기반, 카테고리 옵션) - permitAll, viewer 비로그인 가능
    @GetMapping
    public ResponseEntity<RspTemplate<CursorPageResponse<PostItRspDto>>> getPostIts(
            @RequestParam(required = false) PostCategory categoryCode,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size) {
        Long viewerId = SecurityUtil.getCurrentUserIdOrNull();
        CursorPageResponse<PostItRspDto> data = postItService.getPostIts(categoryCode, cursor, size, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 목록 조회 성공", data));
    }

    // 내가 쓴 글
    @GetMapping("/me")
    public ResponseEntity<RspTemplate<Page<PostItRspDto>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostItRspDto> data = postItService.getMyPosts(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "내 포스트잇 조회 성공", data));
    }

    // 단건 상세 - permitAll, viewer 비로그인 가능
    @GetMapping("/{postId}")
    public ResponseEntity<RspTemplate<PostItRspDto>> getOnePostIt(@PathVariable Long postId) {
        Long viewerId = SecurityUtil.getCurrentUserIdOrNull();
        PostItRspDto data = postItService.getOnePostIt(postId, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 상세 조회 성공", data));
    }

    // Soft delete
    @DeleteMapping("/{postId}")
    public ResponseEntity<RspTemplate<Void>> deletePostIt(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        postItService.deletePostIt(postId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 삭제 성공"));
    }

    // 상단 고정 활성화 - POST_PIN 아이템 인벤토리 소비, 지속시간은 아이템 마스터 기준
    @PostMapping("/{postId}/pin")
    public ResponseEntity<RspTemplate<PostItRspDto>> activatePin(@PathVariable Long postId) {
        Long userId = SecurityUtil.getCurrentUserId();
        PostItRspDto data = postItService.activatePin(postId, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "상단 고정 성공", data));
    }
}

