package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 측 밸런스 댓글 응답
// 본문은 숨김/삭제 정책 미적용 — 원본 그대로 노출 (어드민이 판단해야 하므로).
@Getter
@Builder
public class AdminBalCommentRspDto {

    private Long id;
    private String uuid;
    private String parentId;
    private String content;
    private Long authorUserId;
    private String authorUserNickname;     // users.nickname (BE JOIN)
    private String displayNickname;         // bal_game_comment.nickname (auto-generated)
    private String voteChoice;              // "A" / "B" / null — 댓글 작성자가 이 게임에 한 투표
    private Integer likesCount;
    private Integer reportCount;
    private boolean hidden;
    private boolean deleted;
    private LocalDateTime createTime;

    // reportCount 는 bal_game_comment.report_count 엔티티 필드 직접 사용 (DB 트리거로 동기화됨).
    public static AdminBalCommentRspDto of(BalGameComment c,
                                            String authorUserNickname,
                                            BalVoteChoice voteChoice) {
        return AdminBalCommentRspDto.builder()
                .id(c.getId())
                .uuid(c.getUuid())
                .parentId(c.getParent() == null ? null : c.getParent().getUuid())
                .content(c.getContent())
                .authorUserId(c.getUser() == null ? null : c.getUser().getId())
                .authorUserNickname(authorUserNickname)
                .displayNickname(c.getNickname())
                .voteChoice(voteChoice == null ? null : voteChoice.name())
                .likesCount(c.getLikesCount() == null ? 0 : c.getLikesCount())
                .reportCount(c.getReportCount() == null ? 0 : c.getReportCount())
                .hidden(Boolean.TRUE.equals(c.getIsHidden()))
                .deleted(Boolean.TRUE.equals(c.getIsDeleted()))
                .createTime(c.getCreateTime())
                .build();
    }
}
