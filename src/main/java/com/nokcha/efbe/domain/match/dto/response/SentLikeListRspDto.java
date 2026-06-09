package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 내가 누른 좋아요 페이지 (cursor 기반). */
@Schema(description = "내가 누른 좋아요 페이지")
public record SentLikeListRspDto(
        List<SentLikeItemRspDto> data,
        PageMetaDto meta
) {}
