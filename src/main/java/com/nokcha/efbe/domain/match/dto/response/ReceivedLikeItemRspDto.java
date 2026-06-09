package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** 받은 좋아요 1행. */
@Schema(description = "받은 좋아요 1행")
public record ReceivedLikeItemRspDto(
        @Schema(description = "match_actions.id") String requestId,
        @Schema(description = "create_time ISO") String createdAt,
        @Schema(description = "SUPER_LIKE AND create_time >= NOW-3일") boolean isSuper,
        @Schema(description = "보낸 사람 정보") MatchLikeUserDto fromUser
) {}
