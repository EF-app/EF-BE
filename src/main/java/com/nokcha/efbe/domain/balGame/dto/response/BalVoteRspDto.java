package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// 투표 결과 응답 DTO (% 표시 포함)
@Getter
@Builder
@Schema(description = "투표 결과 (% 표시 포함)")
public class BalVoteRspDto {

    @Schema(description = "게임 PK", example = "1")
    private Long gameId;

    @Schema(description = "내 투표 선택지", example = "A")
    private BalVoteChoice myChoice;

    @Schema(description = "총 투표수 (a_count + b_count)", example = "1000")
    private Integer totalCount;

    @Schema(description = "옵션 A 투표수", example = "620")
    private Integer aCount;

    @Schema(description = "옵션 B 투표수", example = "380")
    private Integer bCount;

    @Schema(description = "옵션 A 비율 (%) — 소수점 첫째 자리", example = "62.0")
    private Double aPercent;

    @Schema(description = "옵션 B 비율 (%) — 소수점 첫째 자리", example = "38.0")
    private Double bPercent;

    public static BalVoteRspDto of(Long gameId, BalVoteChoice myChoice, int total, int a, int b) {
        double aPct = total == 0 ? 0.0 : ((double) a / total) * 100.0;
        double bPct = total == 0 ? 0.0 : ((double) b / total) * 100.0;
        return BalVoteRspDto.builder()
                .gameId(gameId)
                .myChoice(myChoice)
                .totalCount(total)
                .aCount(a)
                .bCount(b)
                .aPercent(Math.round(aPct * 10) / 10.0)
                .bPercent(Math.round(bPct * 10) / 10.0)
                .build();
    }
}
