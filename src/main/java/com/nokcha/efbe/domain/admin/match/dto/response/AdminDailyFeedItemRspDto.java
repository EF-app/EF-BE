package com.nokcha.efbe.domain.admin.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// match_daily_feed row 1건 + viewer/target nickname JOIN
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "일일 피드 1행 (관리자 조회용)")
public class AdminDailyFeedItemRspDto {

    @Schema(description = "피드 날짜")
    private LocalDate feedDate;

    @Schema(description = "뷰어 user.id")
    private Long viewerId;

    @Schema(description = "뷰어 닉네임")
    private String viewerNickname;

    @Schema(description = "matchRank (1~50)")
    private Short matchRank;

    @Schema(description = "타겟 user.id")
    private Long targetId;

    @Schema(description = "타겟 닉네임")
    private String targetNickname;

    @Schema(description = "SCORE/NEWBIE/RANDOM/CUSTOM_KW/FRESH_NEWBIE")
    private String slotType;

    @Schema(description = "sortKey (0~1)")
    private BigDecimal sortKey;

    @Schema(description = "tags_json (JSON 문자열)")
    private String tagsJson;

    @Schema(description = "생성 시각")
    private LocalDateTime createdAt;
}
