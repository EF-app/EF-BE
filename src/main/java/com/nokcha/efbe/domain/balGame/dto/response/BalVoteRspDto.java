package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import lombok.Builder;
import lombok.Getter;

// 투표 결과 응답 DTO (% 표시 포함)
@Getter
@Builder
public class BalVoteRspDto {
    private Long gameId;
    private BalVoteChoice myChoice;
    private Integer totalCount;
    private Integer aCount;
    private Integer bCount;
    private Double aPercent;
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
