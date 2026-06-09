package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/** cursor 페이지네이션 메타. */
@Schema(description = "cursor 페이지네이션 메타")
public record PageMetaDto(
        @Schema(description = "다음 페이지 cursor (마지막 row id). 없으면 null") String nextCursor,
        @Schema(description = "다음 페이지 존재 여부") boolean hasMore
) {}
