package com.nokcha.efbe.domain.admin.balGame.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

// 연령대 / 지역 분포의 한 bucket — 옵션 A/B 각각 투표수.
@Schema(description = "한 그룹의 A/B 투표수")
public record AdminBalVoteBucketStat(
        @Schema(description = "옵션 A 투표수", example = "120") int a,
        @Schema(description = "옵션 B 투표수", example = "90") int b
) {}
