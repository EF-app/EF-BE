package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.chat.dto.response.ChatMessageRspDto;
import com.nokcha.efbe.domain.chat.service.ChatService;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PostIt Reply", description = "포스트잇 답장 진입 API")
@RestController
@RequestMapping("/v1/post-it")
@RequiredArgsConstructor
public class PostReplyController {

    private final ChatService chatService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "포스트잇 답장", description = "id 포스트잇에 답장. 첫 답장이면 채팅방 lazy 생성, isAnonymous=true 면 영원히 익명 유지.")
    @PostMapping("/{postId}/replies")
    public RspTemplate<ChatMessageRspDto> replyToPost(@PathVariable Long postId, @Valid @RequestBody PostReplyReqDto req) {
        Long partnerId = securityUtil.getCurrentUserId();
        ChatMessageRspDto data = chatService.replyToPost(postId, partnerId, req);
        return new RspTemplate<>(HttpStatus.CREATED, "답장 성공", data);
    }
}
