package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

// 밸런스 게임 상세 응답 DTO (홈 진입용 - 최신 댓글 3개 포함)
@Getter
@Builder
@Schema(description = "밸런스 게임 상세")
public class BalGameDetailRspDto {

    @Schema(description = "게임 PK", example = "1")
    private Long id;

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 A 부연설명", example = "출근길 개찰구 앞에서 놓고 온 거 인지함")
    private String optionADesc;

    @Schema(description = "옵션 A 이모지 (UI 장식, 비어있으면 텍스트만)", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 B 부연설명")
    private String optionBDesc;

    @Schema(description = "옵션 B 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "게임 전체 배경 설명")
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "게시 상태", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "예약 게시 시각")
    private LocalDateTime scheduledAt;

    @Schema(description = "예약 종료 시각")
    private LocalDateTime scheduledEndAt;

    @Schema(description = "총 투표수 (a_count + b_count, DB Generated Column)", example = "1000")
    private Integer totalCount;

    @Schema(description = "옵션 A 투표수", example = "620")
    private Integer aCount;

    @Schema(description = "옵션 B 투표수", example = "380")
    private Integer bCount;

    @Schema(description = "댓글 총 개수", example = "328")
    private Integer commentCount;

    // 투표 완료 시 % 계산 결과 (소수점 첫째 자리). 미투표면 null.
    @Schema(description = "옵션 A 비율 (%) — 투표 완료 시에만 채워짐", example = "62.0")
    private Double aPercent;

    @Schema(description = "옵션 B 비율 (%)", example = "38.0")
    private Double bPercent;

    // 내 투표 정보 (없으면 null)
    @Schema(description = "내 투표 선택지 (없으면 null)", example = "A")
    private BalVoteChoice myChoice;

    @Schema(description = "내가 투표했는지 여부")
    private boolean voted;

    // 홈 노출용 최신 댓글 3개 (대댓글 제외)
    @Schema(description = "최신 댓글 미리보기 (대댓글 제외, 최대 3개)")
    private List<CommentRspDto> recentComments;

    @Schema(description = "최초 등록 시각")
    private LocalDateTime createTime;

    @Schema(description = "마지막 변경 시각 (홈 정렬 기준)")
    private LocalDateTime updateTime;

    public static BalGameDetailRspDto of(BalGame g, BalVoteChoice myChoice, List<CommentRspDto> recent) {
        int total = g.getTotalCount() == null ? 0 : g.getTotalCount();
        int a = g.getACount() == null ? 0 : g.getACount();
        int b = g.getBCount() == null ? 0 : g.getBCount();
        double aPct = total == 0 ? 0.0 : ((double) a / total) * 100.0;
        double bPct = total == 0 ? 0.0 : ((double) b / total) * 100.0;
        return BalGameDetailRspDto.builder()
                .id(g.getId())
                .optionA(g.getOptionA())
                .optionADesc(g.getOptionADesc())
                .optionAEmoji(g.getOptionAEmoji())
                .optionB(g.getOptionB())
                .optionBDesc(g.getOptionBDesc())
                .optionBEmoji(g.getOptionBEmoji())
                .description(g.getDescription())
                .categoryCode(g.getCategoryCode())
                .status(g.getStatus())
                .scheduledAt(g.getScheduledAt())
                .scheduledEndAt(g.getScheduledEndAt())
                .totalCount(total)
                .aCount(a)
                .bCount(b)
                .commentCount(g.getCommentCount())
                .aPercent(myChoice == null ? null : Math.round(aPct * 10) / 10.0)
                .bPercent(myChoice == null ? null : Math.round(bPct * 10) / 10.0)
                .myChoice(myChoice)
                .voted(myChoice != null)
                .recentComments(recent)
                .createTime(g.getCreateTime())
                .updateTime(g.getUpdateTime())
                .build();
    }
}
