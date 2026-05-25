package com.nokcha.efbe.domain.balGame.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "댓글/대댓글 생성 요청")
public class CommentCreateReqDto {

    @Schema(description = "댓글 본문", example = "나는 교통카드 두고 오는 게 더 멘붕임")
    @NotBlank
    private String content;

    // 대댓글일 경우 부모 댓글 ID (없으면 null)
    @Schema(description = "대댓글일 경우 부모 댓글 ID (없으면 null — top-level 댓글)", example = "12")
    private Long parentId;
}
