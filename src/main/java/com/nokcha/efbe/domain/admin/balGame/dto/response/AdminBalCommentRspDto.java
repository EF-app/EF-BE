package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 측 밸런스 댓글 응답 — 원본 그대로 노출 (어드민 판단용).
@Getter
@Builder
@Schema(description = "어드민 밸런스 게임 댓글 응답 — 숨김/삭제 글 본문도 원본 노출")
public class AdminBalCommentRspDto {

    @Schema(description = "댓글 PK", example = "1024")
    private Long id;

    @Schema(description = "부모 댓글 PK (대댓글일 때만 채워짐, top-level 이면 null)",
            example = "1020", nullable = true)
    private Long parentId;

    @Schema(description = "댓글 본문 (원본)", example = "재밌네요 ㅋㅋ")
    private String content;

    @Schema(description = "작성자 유저 PK (탈퇴/익명 처리 시 null)", example = "42", nullable = true)
    private Long authorUserId;

    @Schema(description = "작성자 실제 닉네임 (users.nickname, BE JOIN)",
            example = "밤하늘공", nullable = true)
    private String authorUserNickname;

    @Schema(description = "댓글에 표시되는 자동 생성 닉네임 (bal_game_comment.nickname)",
            example = "익명코끼리12")
    private String displayNickname;

    @Schema(description = "댓글 작성자가 이 게임에 한 투표 결과 (A / B / 미투표는 null)",
            example = "A", nullable = true)
    private String voteChoice;

    @Schema(description = "좋아요 수", example = "8")
    private Integer likesCount;

    @Schema(description = "신고 누적 수 (DB 트리거로 동기화)", example = "2")
    private Integer reportCount;

    @Schema(description = "관리자에 의해 숨김 처리된 댓글 여부", example = "false")
    private boolean hidden;

    @Schema(description = "작성자/시스템에 의해 삭제된 댓글 여부 (soft delete)", example = "false")
    private boolean deleted;

    @Schema(description = "작성 시각", example = "2026-05-24T11:30:00")
    private LocalDateTime createTime;

    // reportCount 는 bal_game_comment.report_count 엔티티 필드 직접 사용 (DB 트리거로 동기화됨).
    public static AdminBalCommentRspDto of(BalGameComment c,
                                            String authorUserNickname,
                                            BalVoteChoice voteChoice) {
        return AdminBalCommentRspDto.builder()
                .id(c.getId())
                .parentId(c.getParent() == null ? null : c.getParent().getId())
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
