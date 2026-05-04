package com.nokcha.efbe.domain.postIt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 답장 요청 DTO (첫 답장 시 채팅방 + 첫 메시지 생성)
@Getter
@NoArgsConstructor
public class PostReplyReqDto {

    @NotBlank
    @Size(max = 2000)
    private String content;
}
