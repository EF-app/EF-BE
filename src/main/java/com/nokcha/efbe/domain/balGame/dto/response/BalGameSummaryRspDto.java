package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameSummaryRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 밸런스 게임 목록용 요약 응답 DTO
@Getter
@Builder
@Schema(description = "밸런스 게임 목록용 요약")
public class BalGameSummaryRspDto {

    @Schema(description = "게임 PK", example = "1")
    private Long id;

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 A 부가 설명 (어드민 편집·카드 표시용)")
    private String optionADesc;

    @Schema(description = "옵션 B 부가 설명 (어드민 편집·카드 표시용)")
    private String optionBDesc;

    @Schema(description = "옵션 A 표시용 이모지", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "게시 상태", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "총 투표수 (a_count + b_count)", example = "1000")
    private Integer totalCount;

    @Schema(description = "옵션 A 투표수", example = "620")
    private Integer aCount;

    @Schema(description = "옵션 B 투표수", example = "380")
    private Integer bCount;

    @Schema(description = "댓글 총 개수", example = "328")
    private Integer commentCount;

    @Schema(description = "예약 게시 시각")
    private LocalDateTime scheduledAt;

    @Schema(description = "최초 등록 시각")
    private LocalDateTime createTime;

    public static BalGameSummaryRspDto from(BalGame g) {
        int a = g.getACount() == null ? 0 : g.getACount();
        int b = g.getBCount() == null ? 0 : g.getBCount();
        int comments = g.getCommentCount() == null ? 0 : g.getCommentCount();
        return BalGameSummaryRspDto.builder()
                .id(g.getId())
                .optionA(g.getOptionA())
                .optionB(g.getOptionB())
                .optionADesc(g.getOptionADesc())
                .optionBDesc(g.getOptionBDesc())
                .optionAEmoji(g.getOptionAEmoji())
                .optionBEmoji(g.getOptionBEmoji())
                .categoryCode(g.getCategoryCode())
                .status(g.getStatus())
                .totalCount(a + b)
                .aCount(a)
                .bCount(b)
                .commentCount(comments)
                .scheduledAt(g.getScheduledAt())
                .createTime(g.getCreateTime())
                .build();
    }

    // Querydsl projection 기반 — 신규 피드 표준
    public static BalGameSummaryRspDto from(BalGameSummaryRow r) {
        int a = r.aCount() == null ? 0 : r.aCount();
        int b = r.bCount() == null ? 0 : r.bCount();
        int comments = r.commentCount() == null ? 0 : r.commentCount();
        return BalGameSummaryRspDto.builder()
                .id(r.id())
                .optionA(r.optionA())
                .optionB(r.optionB())
                .optionAEmoji(r.optionAEmoji())
                .optionBEmoji(r.optionBEmoji())
                .categoryCode(r.categoryCode())
                .status(r.status())
                .totalCount(a + b)
                .aCount(a)
                .bCount(b)
                .commentCount(comments)
                .scheduledAt(r.scheduledAt())
                .createTime(r.createTime())
                .build();
    }
}
