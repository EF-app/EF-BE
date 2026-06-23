package com.nokcha.efbe.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "채팅방 메모 수정 요청")
public class ChatMemoReqDto {

    @Size(max = 40)
    @Schema(description = "본인에게만 보이는 채팅방 메모. 빈 문자열 또는 null이면 삭제", example = "다음에 다시 이야기하기", nullable = true)
    private String memo;
}
