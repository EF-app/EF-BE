package com.nokcha.efbe.domain.balGame.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 댓글/대댓글 생성 요청 DTO
@Getter
@NoArgsConstructor
public class CommentCreateReqDto {

    @NotBlank
    private String content;

    // 대댓글일 경우 부모 댓글 ID (없으면 null)
    private Long parentId;
}
