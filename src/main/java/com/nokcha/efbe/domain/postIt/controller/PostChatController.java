package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.security.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.request.PostChatMessageReqDto;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatMessageRspDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatRoomRspDto;
import com.nokcha.efbe.domain.postIt.service.PostChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포스트잇 답장 채팅 RESTful 컨트롤러
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class PostChatController {

    private final PostChatService postChatService;

    // 포스트잇 답장 (첫 답장이면 방 lazy 생성)
    @PostMapping("/post-its/{postId}/replies")
    public ResponseEntity<RspTemplate<PostChatMessageRspDto>> replyToPost(
            @PathVariable Long postId,
            @Valid @RequestBody PostReplyReqDto req) {
        Long partnerId = SecurityUtil.getCurrentUserId();
        PostChatMessageRspDto data = postChatService.replyToPost(postId, partnerId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "답장 성공", data));
    }

    // 내 채팅방 목록
    @GetMapping("/post-chat-rooms")
    public ResponseEntity<RspTemplate<Page<PostChatRoomRspDto>>> getMyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<PostChatRoomRspDto> data = postChatService.getMyRooms(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "채팅방 목록 조회 성공", data));
    }

    // 채팅방 메시지 목록
    @GetMapping("/post-chat-rooms/{roomId}/messages")
    public ResponseEntity<RspTemplate<Page<PostChatMessageRspDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long viewerId = SecurityUtil.getCurrentUserId();
        Page<PostChatMessageRspDto> data = postChatService.getMessages(roomId, viewerId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "메시지 조회 성공", data));
    }

    // 메시지 전송
    @PostMapping("/post-chat-rooms/{roomId}/messages")
    public ResponseEntity<RspTemplate<PostChatMessageRspDto>> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody PostChatMessageReqDto req) {
        Long senderId = SecurityUtil.getCurrentUserId();
        PostChatMessageRspDto data = postChatService.sendMessage(roomId, senderId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "메시지 전송 성공", data));
    }

    // 메시지 취소 (Soft) - read_at 없음 전제
    @DeleteMapping("/post-chat-messages/{messageId}")
    public ResponseEntity<RspTemplate<Void>> cancelMessage(@PathVariable Long messageId) {
        Long requesterId = SecurityUtil.getCurrentUserId();
        postChatService.cancelMessage(messageId, requesterId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "메시지 취소 성공"));
    }
}
