package com.nokcha.efbe.domain.chat.dto.response;

import com.nokcha.efbe.domain.chat.entity.ChatParticipant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "채팅방 메모 응답")
public class ChatMemoRspDto {

    @Schema(description = "채팅방 ID", example = "21")
    private Long chatRoomId;

    @Schema(description = "유저 ID", example = "10")
    private Long userId;

    @Schema(description = "본인에게만 보이는 채팅방 메모", example = "다음에 다시 이야기하기", nullable = true)
    private String memo;

    public static ChatMemoRspDto from(ChatParticipant participant) {
        return ChatMemoRspDto.builder()
                .chatRoomId(participant.getChatRoom().getId())
                .userId(participant.getUser().getId())
                .memo(participant.getMemo())
                .build();
    }
}
