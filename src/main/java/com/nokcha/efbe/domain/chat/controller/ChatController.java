package com.nokcha.efbe.domain.chat.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.chat.dto.request.ChatMemoReqDto;
import com.nokcha.efbe.domain.chat.dto.request.ChatReportLeaveReqDto;
import com.nokcha.efbe.domain.chat.dto.request.ChatRoomCreateReqDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatMemoRspDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatProfileOpenRspDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatRoomRspDto;
import com.nokcha.efbe.domain.chat.service.ChatService;
import com.nokcha.efbe.domain.report.dto.response.ReportRspDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SecurityUtil securityUtil;

    // 내 채팅방 목록
    @Operation(summary = "내 채팅방 목록", description = "본인이 참여 중인 채팅방 목록을 커서 기반으로 조회합니다.")
    @GetMapping
    public RspTemplate<CursorPageResponse<ChatRoomRspDto>> getMyRooms(
            @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비워서 호출") @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수. 기본 20, 최대 50") @RequestParam(required = false) Integer size) {
        Long userId = securityUtil.getCurrentUserId();
        CursorPageResponse<ChatRoomRspDto> data = chatService.getMyRooms(userId, cursor, size);
        return new RspTemplate<>(HttpStatus.OK, "채팅방 목록 조회 성공", data);
    }

    @Operation(summary = "채팅방 생성", description = "MATCH, POWER_MESSAGE 타입의 채팅방을 생성합니다. POST 답장은 /v1/post-it/{postId}/replies 를 사용합니다.")
    @PostMapping
    public RspTemplate<ChatRoomRspDto> createRoom(@Valid @RequestBody ChatRoomCreateReqDto reqDto) {
        Long userId = securityUtil.getCurrentUserId();
        ChatRoomRspDto data = chatService.createRoom(userId, reqDto);
        return new RspTemplate<>(HttpStatus.CREATED, "채팅방이 생성되었습니다.", data);
    }

    @Operation(summary = "채팅 프로필 공개 정보 조회", description = "MATCH 또는 비익명 POST 채팅방에서 현재 공개 단계에 따라 상대방 프로필을 조회합니다.")
    @GetMapping("/{roomId}/profile-open")
    public RspTemplate<ChatProfileOpenRspDto> getProfileOpen(@PathVariable Long roomId) {
        Long userId = securityUtil.getCurrentUserId();
        ChatProfileOpenRspDto data = chatService.getProfileOpen(roomId, userId);
        return new RspTemplate<>(HttpStatus.OK, "프로필 공개 정보 조회 성공", data);
    }

    @Operation(summary = "채팅 프로필 공개 단계 증가", description = "Firebase 채팅에서 10개 단위 메시지 조건을 만족했을 때 프론트가 호출합니다. MATCH 또는 비익명 POST 채팅방 참여자만 호출할 수 있으며 최대 4단계까지만 증가합니다.")
    @PatchMapping("/{roomId}/profile-open/advance")
    public RspTemplate<ChatProfileOpenRspDto> advanceProfileOpenLevel(@PathVariable Long roomId) {
        Long userId = securityUtil.getCurrentUserId();
        ChatProfileOpenRspDto data = chatService.advanceProfileOpenLevel(roomId, userId);
        return new RspTemplate<>(HttpStatus.OK, "프로필 공개 단계 증가 성공", data);
    }

    @Operation(summary = "채팅방 메모 수정", description = "요청한 사용자의 채팅방 참여 정보에 본인만 볼 수 있는 메모를 저장합니다. 빈 문자열 또는 null이면 메모를 삭제합니다.")
    @PatchMapping("/{roomId}/memo")
    public RspTemplate<ChatMemoRspDto> updateMemo(@PathVariable Long roomId, @Valid @RequestBody ChatMemoReqDto reqDto) {
        Long userId = securityUtil.getCurrentUserId();
        ChatMemoRspDto data = chatService.updateMemo(roomId, userId, reqDto);
        return new RspTemplate<>(HttpStatus.OK, "채팅방 메모 수정 성공", data);
    }

    @Operation(summary = "채팅방 나가기", description = "요청한 사용자의 참여 정보를 나가기 상태로 변경합니다. 상대방의 채팅방 목록에는 영향을 주지 않습니다.")
    @DeleteMapping("/{roomId}/leave")
    public RspTemplate<Void> leaveRoom(@PathVariable Long roomId) {
        Long userId = securityUtil.getCurrentUserId();
        chatService.leaveRoom(roomId, userId);
        return new RspTemplate<>(HttpStatus.OK, "채팅방을 나갔습니다.");
    }

    @Operation(summary = "채팅 메시지 신고 후 나가기", description = "채팅방을 CHAT 대상으로 신고하고, Firebase 메시지 ID/본문 스냅샷을 증거로 저장한 뒤 기존 나가기 로직과 동일하게 방을 나갑니다.")
    @PostMapping("/{roomId}/report/leave")
    public RspTemplate<ReportRspDto> reportAndLeaveRoom(@PathVariable Long roomId, @Valid @RequestBody ChatReportLeaveReqDto reqDto) {
        Long userId = securityUtil.getCurrentUserId();
        ReportRspDto data = chatService.reportAndLeaveRoom(roomId, userId, reqDto);
        return new RspTemplate<>(HttpStatus.CREATED, "신고가 접수되었고 채팅방을 나갔습니다.", data);
    }
}
