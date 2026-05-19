package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.request.PostCreateReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostItRspDto;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.service.PostItService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포스트잇 RESTful 컨트롤러
@Tag(name = "PostIt Info", description = "포스트잇 API")
@RestController
@RequestMapping("/v1/post-it")
@RequiredArgsConstructor
public class PostItController {

    private final PostItService postItService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "포스트잇 작성", description = "포스트잇을 작성합니다. 수정은 불가해요.")
    @PostMapping
    public ResponseEntity<RspTemplate<PostItRspDto>> createPostIt(@Valid @RequestBody PostCreateReqDto req) {
        Long userId = securityUtil.getCurrentUserId();
        PostItRspDto data = postItService.createPostIt(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "포스트잇 작성 성공", data));
    }

    @Operation(summary = "포스트잇 피드 조회", description = "커서 기반, 카테고리 옵션")
    @GetMapping
    public ResponseEntity<RspTemplate<CursorPageResponse<PostItRspDto>>> getPostIts(
            @RequestParam(required = false) PostCategory categoryCode,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size) {
        Long viewerId = securityUtil.getCurrentUserIdOrNull();
        CursorPageResponse<PostItRspDto> data = postItService.getPostIts(categoryCode, cursor, size, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 목록 조회 성공", data));
    }

    @Operation(summary = "포스트잇 단건 상세", description = "포스트잇 단건을 외부 식별자(uuid)로 상세 조회합니다. 비로그인도 호출 가능.")
    @GetMapping("/{uuid}")
    public ResponseEntity<RspTemplate<PostItRspDto>> getOnePostIt(@PathVariable String uuid) {
        Long viewerId = securityUtil.getCurrentUserIdOrNull();
        PostItRspDto data = postItService.getOnePostIt(uuid, viewerId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 상세 조회 성공", data));
    }

    @Operation(summary = "포스트잇 삭제", description = "본인 포스트잇을 uuid 로 Soft delete. 연결된 채팅방은 활성 유지.")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<RspTemplate<Void>> deletePostIt(@PathVariable String uuid) {
        Long userId = securityUtil.getCurrentUserId();
        postItService.deletePostIt(uuid, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "포스트잇 삭제 성공"));
    }

    @Operation(summary = "포스트잇 상단 고정", description = "POST_PIN 아이템을 소비해 본인 포스트잇(uuid)을 일정 시간 동안 상단 고정.")
    @PostMapping("/{uuid}/pin")
    public ResponseEntity<RspTemplate<PostItRspDto>> activatePin(@PathVariable String uuid) {
        Long userId = securityUtil.getCurrentUserId();
        PostItRspDto data = postItService.activatePin(uuid, userId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "상단 고정 성공", data));
    }
}

