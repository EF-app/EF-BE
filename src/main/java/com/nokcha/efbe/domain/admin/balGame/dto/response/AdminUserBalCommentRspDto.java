package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자 유저 상세 - 유저가 작성한 밸런스 게임 댓글 응답")
public class AdminUserBalCommentRspDto {

    @Schema(description = "댓글 ID", example = "101")
    private Long id;

    @Schema(description = "밸런스 게임 ID", example = "15")
    private Long gameId;

    @Schema(description = "게임 옵션 A", example = "평생 에어컨 없이 살기")
    private String gameOptionA;

    @Schema(description = "게임 옵션 B", example = "평생 히터 없이 살기")
    private String gameOptionB;

    @Schema(description = "댓글 내용", example = "저는 그래도 히터 없는 쪽이 나아요.")
    private String content;

    @Schema(description = "작성자의 해당 게임 투표 선택지. 현재 미집계라 null 일 수 있음.", example = "A", nullable = true)
    private String voteChoice;

    @Schema(description = "댓글 좋아요 수", example = "12")
    private Integer likeCount;

    @Schema(description = "댓글 답글 수. 현재 미집계라 기본값 0", example = "0")
    private Integer replyCount;

    @Schema(description = "댓글 신고 수", example = "2")
    private Integer reportCount;

    @Schema(description = "관리자에 의해 숨김 처리되었는지 여부", example = "false")
    private boolean hidden;

    @Schema(description = "삭제 처리된 댓글인지 여부", example = "false")
    private boolean deleted;

    @Schema(description = "댓글 작성 시각", example = "2026-05-25T14:30:00")
    private LocalDateTime createTime;

    public static AdminUserBalCommentRspDto from(BalGameComment c) {
        BalGame game = c.getGame();
        return AdminUserBalCommentRspDto.builder()
                .id(c.getId())
                .gameId(game == null ? null : game.getId())
                .gameOptionA(game == null ? null : game.getOptionA())
                .gameOptionB(game == null ? null : game.getOptionB())
                .content(c.getContent())
                .voteChoice(null)
                .likeCount(c.getLikesCount() == null ? 0 : c.getLikesCount())
                .replyCount(0)
                .reportCount(c.getReportCount() == null ? 0 : c.getReportCount())
                .hidden(Boolean.TRUE.equals(c.getIsHidden()))
                .deleted(Boolean.TRUE.equals(c.getIsDeleted()))
                .createTime(c.getCreateTime())
                .build();
    }
}
