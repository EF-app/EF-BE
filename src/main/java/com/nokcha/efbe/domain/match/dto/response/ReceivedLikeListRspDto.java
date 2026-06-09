package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 받은 좋아요 페이지 (cursor 기반). */
@Schema(description = "받은 좋아요 페이지")
public record ReceivedLikeListRspDto(
        List<ReceivedLikeItemRspDto> data,
        PageMetaDto meta
) {}
