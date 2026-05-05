package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// 댓글/대댓글 응답 DTO (계층 구조 포함)
@Getter
@Builder
public class CommentRspDto {
    private Long id;
    private Long parentId;
    private String nickname;
    private String content;
    private boolean deleted;
    private boolean hidden;
    private Integer likesCount;
    private boolean likedByMe;
    private boolean ownedByMe;
    private LocalDateTime createTime;
    // 대댓글 목록 (없으면 빈 리스트)
    private List<CommentRspDto> children;

    // 표시 정책 적용된 단일 댓글 변환 (children 은 외부에서 채움)
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
