package com.nokcha.efbe.domain.postIt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 답장 요청 DTO (첫 답장 시 채팅방 + 첫 메시지 생성)
// isAnonymous=true 면 첫 답장 시 그 방은 영원히 익명 (이후 토글 불가). 같은 방의 모든 메시지에 일관 적용.
@Getter
@NoArgsConstructor
@Schema(description = "포스트잇 답장 요청 — 첫 답장 시 채팅방이 lazy 생성되며, 그 시점의 isAnonymous 가 방의 익명 정책으로 영구 고정됨")
public class PostReplyReqDto {

    @NotBlank
    @Size(max = 2000)
    @Schema(description = "답장 본문 (최대 2000자)", example = "안녕하세요, 답장 드려요!")
    private String content;

    @Schema(description = "익명 답장 여부 (첫 답장 시에만 의미 있음, 이후 메시지는 방 정책 자동 따름)", example = "false")
    private Boolean isAnonymous;
}
