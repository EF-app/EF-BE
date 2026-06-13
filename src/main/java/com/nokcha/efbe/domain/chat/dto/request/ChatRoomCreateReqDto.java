package com.nokcha.efbe.domain.chat.dto.request;

import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅방 생성 요청")
public class ChatRoomCreateReqDto {

    @NotNull(message = "채팅방 타입은 필수입니다.")
    @Schema(description = "채팅방 타입", example = "POST")
    private ChatRoomType roomType;

    @Schema(description = "MATCH/POWER_MESSAGE 방 생성 시 상대 유저 ID", example = "14")
    private Long targetUserId;

    @Schema(description = "MATCH 방 생성 시 매칭 결과 ID", example = "12")
    private Long matchResultId;

    @Size(max = 2000, message = "파워메시지는 2000자 이하로 입력해야 합니다.")
    @Schema(description = "POWER_MESSAGE 방 생성 시 첫 대화 내용", example = "안녕하세요. 대화해보고 싶어서 메시지 보냈어요.")
    private String powerMessage;
}
