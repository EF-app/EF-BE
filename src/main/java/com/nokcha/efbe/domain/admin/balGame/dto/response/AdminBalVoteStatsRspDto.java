package com.nokcha.efbe.domain.admin.balGame.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// 어드민 측 밸런스 게임 투표 통계.
// AdminBalGameDetailRspDto.voteStats 에 nested 로 포함.
@Getter
@Builder
@Schema(description = "어드민 밸런스 게임 투표 통계")
public class AdminBalVoteStatsRspDto {

    @Schema(description = "옵션 A 비율 (%)", example = "62.0")
    private Double aPercent;

    @Schema(description = "옵션 B 비율 (%)", example = "38.0")
    private Double bPercent;

    @Schema(description = "연령대 분포 (5단위: 20~24/25~29/30~34/35~39/40~44/45~49/50대 이상/미설정)")
    private Map<String, AdminBalVoteBucketStat> ageDistribution;

    @Schema(description = "지역 분포 (country 단위, null 은 '미설정')")
    private Map<String, AdminBalVoteBucketStat> areaDistribution;
}
