package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// match_daily_feed row 1건 + viewer/target nickname JOIN
@Schema(description = "일일 피드 1행 (관리자 조회용)")
public record AdminDailyFeedItemRspDto(
        @Schema(description = "피드 날짜")              LocalDate feedDate,
        @Schema(description = "뷰어 user.id")           Long viewerId,
        @Schema(description = "뷰어 닉네임")             String viewerNickname,
        @Schema(description = "matchRank (1~50)")      Short matchRank,
        @Schema(description = "타겟 user.id")           Long targetId,
        @Schema(description = "타겟 닉네임")             String targetNickname,
        @Schema(description = "SCORE/NEWBIE/RANDOM/CUSTOM_KW/FRESH_NEWBIE") String slotType,
        @Schema(description = "sortKey (0~1)")          BigDecimal sortKey,
        @Schema(description = "tags_json (JSON 문자열)") String tagsJson,
        @Schema(description = "생성 시각")               LocalDateTime createdAt
) {}
