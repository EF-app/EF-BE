package com.nokcha.efbe.domain.postIt.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "포스트잇 좋아요 토글 응답")
public class PostLikeRspDto {

    @Schema(description = "포스트잇 ID", example = "42")
    private Long postId;

    @Schema(description = "토글 직후 누적 좋아요 수", example = "13")
    private long likeCount;

    @Schema(description = "현재 로그인 유저의 좋아요 여부", example = "true")
    private boolean likedByMe;
}
