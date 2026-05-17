package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// "내 활동 — 밸런스게임" 카드 응답 DTO
// - 게임 본문 + 내 투표 결과 + 양 옵션 투표수 + 댓글수
// - totalCount 는 a_count + b_count 합산값 (BE 계산). 투표 비율(%) 계산은 FE 에서.
@Getter
@Builder
@Schema(description = "내 활동 — 내가 투표한 밸런스게임 카드")
public class BalGameUserActivityEntryRspDto {

    @Schema(description = "게임 PK", example = "1")
    private Long gameId;

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 A 표시용 이모지", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "게시 상태 (PUBLISHED/ARCHIVED 만 노출)", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "총 투표수 (a_count + b_count)", example = "1000")
    private Integer totalCount;

    @Schema(description = "옵션 A 투표수", example = "620")
    private Integer aCount;

    @Schema(description = "옵션 B 투표수", example = "380")
    private Integer bCount;

    @Schema(description = "댓글 총 개수", example = "328")
    private Integer commentCount;

    @Schema(description = "내 투표 결과 (A 또는 B)", example = "A")
    private BalVoteChoice myChoice;

    @Schema(description = "내가 투표한 시각")
    private LocalDateTime myVotedAt;

    @Schema(description = "게임 최초 등록 시각")
    private LocalDateTime gameCreateTime;

    public static BalGameUserActivityEntryRspDto from(BalGameUserActivityEntryRow r) {
        int a = r.aCount() == null ? 0 : r.aCount();
        int b = r.bCount() == null ? 0 : r.bCount();
        int comments = r.commentCount() == null ? 0 : r.commentCount();
        return BalGameUserActivityEntryRspDto.builder()
                .gameId(r.gameId())
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
                .myChoice(r.myChoice())
                .myVotedAt(r.myVotedAt())
                .gameCreateTime(r.gameCreateTime())
                .build();
    }
}
