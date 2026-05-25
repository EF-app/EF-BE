package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@Schema(description = "댓글/대댓글 (계층 구조 포함)")
public class CommentRspDto {

    @Schema(description = "댓글 PK", example = "10")
    private Long id;

    @Schema(description = "부모 댓글 ID — 대댓글일 경우만 채워짐 (top-level 이면 null)", example = "5")
    private Long parentId;

    @Schema(description = "작성자 닉네임", example = "용감한 다람쥐")
    private String nickname;

    @Schema(description = "표시될 본문 (삭제/숨김 정책 적용된 결과)")
    private String content;

    @Schema(description = "삭제된 댓글 여부")
    private boolean deleted;

    @Schema(description = "숨김 처리된 댓글 여부")
    private boolean hidden;

    @Schema(description = "좋아요 수", example = "12")
    private Integer likesCount;

    @Schema(description = "내가 좋아요한 댓글 여부")
    private boolean likedByMe;

    @Schema(description = "내가 작성한 댓글 여부")
    private boolean ownedByMe;

    @Schema(description = "작성 시각")
    private LocalDateTime createTime;

    @Schema(description = "대댓글 목록 (없으면 빈 리스트)")
    private List<CommentRspDto> children;

    public static CommentRspDto from(BalGameComment c, Long viewerId, boolean likedByMe) {
        boolean owned = c.getUser() != null && viewerId != null && viewerId.equals(c.getUser().getId());
        return CommentRspDto.builder()
                .id(c.getId())
                .parentId(c.getParent() == null ? null : c.getParent().getId())
                .nickname(c.getNickname())
                .content(c.resolveDisplayContent())
                .deleted(Boolean.TRUE.equals(c.getIsDeleted()))
                .hidden(Boolean.TRUE.equals(c.getIsHidden()))
                .likesCount(c.getLikesCount())
                .likedByMe(likedByMe)
                .ownedByMe(owned)
                .createTime(c.getCreateTime())
                .children(new ArrayList<>())
                .build();
    }
}
