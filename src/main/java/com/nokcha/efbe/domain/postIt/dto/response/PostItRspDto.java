package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 응답 DTO (삭제/숨김 표시 정책 + 익명 처리 반영)
// 익명 마스킹 정책:
//   - 모든 viewing(피드/내가 쓴 글/단건 조회) 응답에서는 익명 글이면 userId=null (frontend 가 "from 익명" 표시)
//   - 본인 시점도 동일 — "메인 피드에서 본인 글이라도 닉네임 노출 금지" 요구사항 충족
//   - 단, 작성/owner 액션 응답(createPostIt, activatePin) 만 fromOwnerView 로 userId 노출 (본인 확인용)
//   - 신고 처리 등 admin 흐름은 별도 admin DTO 사용 (본 DTO 와 무관)
@Getter
@Builder
public class PostItRspDto {
    private Long id;
    private Long userId;         // 익명이면 null (단 fromOwnerView 경로는 노출)
    private PostCategory categoryCode;
    private String content;
    private boolean anonymous;
    private boolean lightning;
    private LocalDateTime expiresAt;
    private LocalDateTime pinnedUntil;
    private boolean pinned;
    private Integer replyCount;
    private boolean hidden;
    private boolean deleted;
    private LocalDateTime createTime;

    // viewing 기본값 — 익명이면 userId 마스킹
    public static PostItRspDto from(PostIt p) {
        boolean anonymous = Boolean.TRUE.equals(p.getIsAnonymous());
        Long authorId = p.getUser() == null ? null : p.getUser().getId();
        return PostItRspDto.builder()
                .id(p.getId())
                .userId(anonymous ? null : authorId)
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
                .anonymous(anonymous)
                .lightning(p.isLightning())
                .expiresAt(p.getExpiresAt())
                .pinnedUntil(p.getPinnedUntil())
                .pinned(p.isPinned())
                .replyCount(p.getReplyCount())
                .hidden(Boolean.TRUE.equals(p.getIsHidden()))
                .deleted(Boolean.TRUE.equals(p.getIsDeleted()))
                .createTime(p.getCreateTime())
                .build();
    }

    // Querydsl projection 기반 — 신규 피드 표준
    // 표시 정책 (삭제/숨김 치환) 을 row 단계에서 적용. 익명이면 userId 마스킹.
    public static PostItRspDto from(PostItRow r) {
        boolean anonymous = Boolean.TRUE.equals(r.isAnonymous());
        boolean hidden = Boolean.TRUE.equals(r.isHidden());
        boolean deleted = Boolean.TRUE.equals(r.isDeleted());
        String content = hidden ? PostIt.HIDDEN_POST_TEXT
                : deleted ? PostIt.DELETED_POST_TEXT
                : r.content();
        boolean pinned = r.pinnedUntil() != null && r.pinnedUntil().isAfter(LocalDateTime.now());
        return PostItRspDto.builder()
                .id(r.id())
                .userId(anonymous ? null : r.userId())
                .categoryCode(r.categoryCode())
                .content(content)
                .anonymous(anonymous)
                .lightning(r.categoryCode() == PostCategory.LIGHTN)
                .expiresAt(r.expiresAt())
                .pinnedUntil(r.pinnedUntil())
                .pinned(pinned)
                .replyCount(r.replyCount())
                .hidden(hidden)
                .deleted(deleted)
                .createTime(r.createTime())
                .build();
    }

    // 작성/owner 액션 응답 — 익명이어도 userId 그대로 노출. 본인 확인용.
    public static PostItRspDto fromOwnerView(PostIt p) {
        boolean anonymous = Boolean.TRUE.equals(p.getIsAnonymous());
        Long authorId = p.getUser() == null ? null : p.getUser().getId();
        return PostItRspDto.builder()
                .id(p.getId())
                .userId(authorId)
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
                .anonymous(anonymous)
                .lightning(p.isLightning())
                .expiresAt(p.getExpiresAt())
                .pinnedUntil(p.getPinnedUntil())
                .pinned(p.isPinned())
                .replyCount(p.getReplyCount())
                .hidden(Boolean.TRUE.equals(p.getIsHidden()))
                .deleted(Boolean.TRUE.equals(p.getIsDeleted()))
                .createTime(p.getCreateTime())
                .build();
    }
}
