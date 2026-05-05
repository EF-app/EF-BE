package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameSummaryRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 밸런스 게임 목록용 요약 응답 DTO
@Getter
@Builder
public class BalGameSummaryRspDto {
    private Long id;
    private String optionA;
    private String optionB;
    private String optionAEmoji;
    private String optionBEmoji;
    private BalCategoryCode categoryCode;
    private BalGameStatus status;
    private Integer totalCount;
    private Integer aCount;
    private Integer bCount;
    private Integer commentCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime createTime;

    public static BalGameSummaryRspDto from(BalGame g) {
        return BalGameSummaryRspDto.builder()
                .id(g.getId())
                .optionA(g.getOptionA())
                .optionB(g.getOptionB())
                .optionAEmoji(g.getOptionAEmoji())
                .optionBEmoji(g.getOptionBEmoji())
                .categoryCode(g.getCategoryCode())
                .status(g.getStatus())
                .totalCount(g.getTotalCount())
                .aCount(g.getACount())
                .bCount(g.getBCount())
                .commentCount(g.getCommentCount())
                .scheduledAt(g.getScheduledAt())
                .createTime(g.getCreateTime())
                .build();
    }

    // Querydsl projection 기반 — 신규 피드 표준
    public static BalGameSummaryRspDto from(BalGameSummaryRow r) {
        return BalGameSummaryRspDto.builder()
                .id(r.id())
                .optionA(r.optionA())
                .optionB(r.optionB())
                .optionAEmoji(r.optionAEmoji())
                .optionBEmoji(r.optionBEmoji())
                .categoryCode(r.categoryCode())
                .status(r.status())
                .totalCount(r.totalCount())
                .aCount(r.aCount())
                .bCount(r.bCount())
                .commentCount(r.commentCount())
                .scheduledAt(r.scheduledAt())
                .createTime(r.createTime())
                .build();
    }
}
