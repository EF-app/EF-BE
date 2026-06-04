package com.nokcha.efbe.domain.chat.dto.response;

import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "채팅방 응답")
public class ChatRoomRspDto {

    @Schema(description = "채팅방 ID", example = "21")
    private Long id;

    @Schema(description = "채팅방 UUID", example = "1f0a2e5b-6f38-4e91-b8c2-12c2a8c6d9e1")
    private String uuid;

    @Schema(description = "원본 포스트잇 ID", example = "77", nullable = true)
    private Long postId;

    @Schema(description = "Firebase 채팅방 ID", example = "chat_1f0a2e5b")
    private String firebaseId;

    @Schema(description = "채팅방 타입", example = "POST")
    private ChatRoomType roomType;

    @Schema(description = "포스트잇 원글 내용 스냅샷", nullable = true)
    private String postContentSnapshot;

    @Schema(description = "파워메시지 첫 대화 내용", nullable = true)
    private String powerMessage;

    @Schema(description = "파워메시지 상단 고정 만료 시각", nullable = true)
    private LocalDateTime powerPinnedUntil;

    @Schema(description = "매칭 결과 ID", example = "12", nullable = true)
    private Long matchResultId;

    @Schema(description = "페어 유저 A ID. 두 유저 ID 중 작은 값", example = "10")
    private Long pairUserAId;

    @Schema(description = "페어 유저 B ID. 두 유저 ID 중 큰 값", example = "14")
    private Long pairUserBId;

    @Schema(description = "활성 채팅방 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "익명 채팅방 여부", example = "false")
    private Boolean isAnonymous;

    @Schema(description = "채팅방 생성 시각", example = "2026-05-25T16:00:00")
    private LocalDateTime createTime;

    public static ChatRoomRspDto from(ChatRoom r) {
        return ChatRoomRspDto.builder()
                .id(r.getId())
                .uuid(r.getUuid())
                .postId(r.getPost() == null ? null : r.getPost().getId())
                .firebaseId(r.getFirebaseId())
                .roomType(r.getRoomType())
                .postContentSnapshot(r.getPostContentSnapshot())
                .powerMessage(r.getPowerMessage())
                .powerPinnedUntil(r.getPowerPinnedUntil())
                .matchResultId(r.getMatchResultId())
                .pairUserAId(r.getPairUserAId())
                .pairUserBId(r.getPairUserBId())
                .isActive(Boolean.TRUE.equals(r.getIsActive()))
                .isAnonymous(Boolean.TRUE.equals(r.getIsAnonymous()))
                .createTime(r.getCreateTime())
                .build();
    }
}
