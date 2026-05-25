package com.nokcha.efbe.domain.postIt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "포스트잇 채팅 메시지 전송 요청")
public class PostChatMessageReqDto {

    @NotBlank
    @Size(max = 2000)
    @Schema(description = "메시지 본문 (최대 2000자)", example = "안녕하세요, 메시지 보내요!")
    private String content;
}
