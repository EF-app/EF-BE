package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 — 특정 유저가 작성한 밸런스 게임 댓글
@Getter
@Builder
public class AdminUserBalCommentRspDto {

    private Long id;
    private Long gameId;
    private String gameOptionA;
    private String gameOptionB;
    private String content;
    private String voteChoice;          // 작성자의 해당 게임 투표 — 현재 미집계, null
    private Integer likeCount;
    private Integer replyCount;          // 현재 미집계, 0
    private Integer reportCount;
    private boolean hidden;
    private boolean deleted;
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
