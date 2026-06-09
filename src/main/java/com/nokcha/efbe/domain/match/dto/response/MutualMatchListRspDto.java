package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 서로 좋아요 페이지 (cursor 기반). */
@Schema(description = "서로 좋아요 페이지")
public record MutualMatchListRspDto(
        List<MutualMatchItemRspDto> data,
        PageMetaDto meta
) {}
