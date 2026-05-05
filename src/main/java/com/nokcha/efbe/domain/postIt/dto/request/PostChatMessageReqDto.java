package com.nokcha.efbe.domain.postIt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 채팅 메시지 요청 DTO
@Getter
@NoArgsConstructor
public class PostChatMessageReqDto {

    @NotBlank
    @Size(max = 2000)
    private String content;
}
