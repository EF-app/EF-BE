package com.nokcha.efbe.domain.postIt.controller;

import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.postIt.dto.request.PostChatMessageReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatMessageRspDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatRoomRspDto;
import com.nokcha.efbe.domain.postIt.service.PostChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 포스트잇 답장 채팅 RESTful 컨트롤러
@Tag(name = "PostIt Chat", description = "포스트잇 답장 채팅 API")
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class PostChatController {

    private final PostChatService postChatService;
    private final SecurityUtil securityUtil;

    // 내 채팅방 목록
    @Operation(summary = "내 채팅방 목록", description = "본인이 참여 중인 포스트잇 답장 채팅방 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/post-chat-rooms")
    public ResponseEntity<RspTemplate<Page<PostChatRoomRspDto>>> getMyRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = securityUtil.getCurrentUserId();
        Page<PostChatRoomRspDto> data = postChatService.getMyRooms(userId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "채팅방 목록 조회 성공", data));
    }

    // 채팅방 메시지 목록
    @Operation(summary = "채팅방 메시지 목록", description = "특정 채팅방의 메시지 목록을 페이지네이션으로 조회합니다. (참여자만 가능)")
    @GetMapping("/post-chat-rooms/{roomId}/messages")
    public ResponseEntity<RspTemplate<Page<PostChatMessageRspDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long viewerId = securityUtil.getCurrentUserId();
        Page<PostChatMessageRspDto> data = postChatService.getMessages(roomId, viewerId, page, size);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "메시지 조회 성공", data));
    }

    // 메시지 전송
    @Operation(summary = "채팅 메시지 전송", description = "기존 채팅방에 새 메시지를 전송합니다. 익명 정책은 방 생성 시 결정된 값을 그대로 따릅니다.")
    @PostMapping("/post-chat-rooms/{roomId}/messages")
    public ResponseEntity<RspTemplate<PostChatMessageRspDto>> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody PostChatMessageReqDto req) {
        Long senderId = securityUtil.getCurrentUserId();
        PostChatMessageRspDto data = postChatService.sendMessage(roomId, senderId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RspTemplate<>(HttpStatus.CREATED, "메시지 전송 성공", data));
    }

    // 메시지 취소 (Soft) - read_at 없음 전제
    @Operation(summary = "채팅 메시지 취소", description = "발신자 본인이 보낸 메시지를 Soft delete 합니다. 상대가 읽지 않은(read_at 없음) 경우에만 가능합니다.")
    @DeleteMapping("/post-chat-messages/{messageId}")
    public ResponseEntity<RspTemplate<Void>> cancelMessage(@PathVariable Long messageId) {
        Long requesterId = securityUtil.getCurrentUserId();
        postChatService.cancelMessage(messageId, requesterId);
        return ResponseEntity.ok(new RspTemplate<>(HttpStatus.OK, "메시지 취소 성공"));
    }
}
