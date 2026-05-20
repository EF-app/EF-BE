package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatMessageRspDto;
import com.nokcha.efbe.domain.postIt.service.PostChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 포스트잇 답장 진입점 — URL 이 /post-its/{postId}/sub-resource 라 post-it 패키지 잔류.
// 비즈니스(채팅방 lazy 생성 + 첫 메시지 저장)는 PostChatService 그대로 위임.
@Tag(name = "PostIt Reply", description = "포스트잇 답장 진입 API")
@RestController
@RequestMapping("/v1/post-it")
@RequiredArgsConstructor
public class PostReplyController {

    private final PostChatService postChatService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "포스트잇 답장", description = "id 포스트잇에 답장. 첫 답장이면 채팅방 lazy 생성, isAnonymous=true 면 영원히 익명 유지.")
    @PostMapping("/{postId}/replies")
    public ResponseEntity<RspTemplate<PostChatMessageRspDto>> replyToPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostReplyReqDto req) {
        Long partnerId = securityUtil.getCurrentUserId();
        PostChatMessageRspDto data = postChatService.replyToPost(postId, partnerId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "답장 성공", data));
    }
}
