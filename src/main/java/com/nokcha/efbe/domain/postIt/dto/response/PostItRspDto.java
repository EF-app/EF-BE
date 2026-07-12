package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.common.util.DisplayNameUtil;
import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포스트잇 응답")
public class PostItRspDto {
    public static final String ANONYMOUS_NICKNAME = "익명";

    @Schema(description = "포스트잇 ID", example = "42")
    private Long id;

    @Schema(description = "작성자 userId. 익명 글이면 null이며, owner 전용 응답에서는 노출될 수 있음.", example = "15", nullable = true)
    private Long userId;

    @Schema(description = "표시용 닉네임. 익명 글이면 '익명'.", example = "밤하늘공")
    private String nickname;

    @Schema(description = "작성자 나이(한국 나이). 익명 글이면 null.", example = "27", nullable = true)
    private Integer age;

    @Schema(description = "작성자 지역. 익명 글이면 null.", example = "서울특별시 강남구", nullable = true)
    private String location;

    @Schema(description = "포스트잇 카테고리", example = "DAILY")
    private PostCategory categoryCode;

    @Schema(description = "표시용 본문. 숨김/삭제 상태면 치환 문구가 내려갈 수 있음.", example = "오늘 하루가 너무 길었다.")
    private String content;

    @Schema(description = "포스트잇 색상", example = "YELLOW")
    private PostItColor color;

    @Schema(description = "익명 글 여부", example = "false")
    private boolean anonymous;

    @Schema(description = "번개 카테고리 여부", example = "false")
    private boolean lightning;

    @Schema(description = "만료 시각", example = "2026-05-25T23:59:59", nullable = true)
    private LocalDateTime expiresAt;

    @Schema(description = "상단 고정 만료 시각", example = "2026-05-25T18:00:00", nullable = true)
    private LocalDateTime pinnedUntil;

    @Schema(description = "현재 상단 고정 활성 여부", example = "true")
    private boolean pinned;

    @Schema(description = "답글 수", example = "3")
    private Integer replyCount;

    @Schema(description = "좋아요 수", example = "12")
    private long likeCount;

    @Schema(description = "현재 로그인 유저가 좋아요를 눌렀는지 여부", example = "true")
    private boolean likedByMe;

    @Schema(description = "관리자에 의해 숨김 처리된 글 여부", example = "false")
    private boolean hidden;

    @Schema(description = "삭제 처리된 글 여부", example = "false")
    private boolean deleted;

    @Schema(description = "현재 조회 유저가 작성자 본인인지 여부. 익명 글이어도 본인이면 true.", example = "false")
    private boolean mine;

    @Schema(description = "작성 시각", example = "2026-05-25T14:30:00")
    private LocalDateTime createTime;

    public static PostItRspDto from(PostIt p, long likeCount, boolean likedByMe, String areaCountry, String areaCity, Long viewerId) {
        boolean anonymous = Boolean.TRUE.equals(p.getIsAnonymous());
        Long authorId = p.getUser() == null ? null : p.getUser().getId();
        String authorNickname = p.getUser() == null ? null : p.getUser().getNickname();
        Integer authorAge = p.getUser() == null ? null : p.getUser().getAge();
        boolean owner = viewerId != null && authorId != null && viewerId.equals(authorId);
        return PostItRspDto.builder()
                .id(p.getId())
                .userId(anonymous ? null : authorId)
                .nickname(resolveNickname(anonymous, authorNickname))
                .age(anonymous ? null : authorAge)
                .location(anonymous ? null : LocationUtil.composeLocation(areaCountry, areaCity))
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
                .color(p.getColor())
                .anonymous(anonymous)
                .lightning(p.isLightning())
                .expiresAt(p.getExpiresAt())
                .pinnedUntil(p.getPinnedUntil())
                .pinned(p.isPinned())
                .replyCount(p.getReplyCount())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .hidden(Boolean.TRUE.equals(p.getIsHidden()))
                .deleted(Boolean.TRUE.equals(p.getIsDeleted()))
                .mine(owner)
                .createTime(p.getCreateTime())
                .build();
    }

    // Querydsl projection 기반 — 신규 피드 표준
    public static PostItRspDto from(PostItRow r, Long viewerId) {
        boolean anonymous = Boolean.TRUE.equals(r.isAnonymous());
        boolean hidden = Boolean.TRUE.equals(r.isHidden());
        boolean deleted = Boolean.TRUE.equals(r.isDeleted());
        String content = hidden ? PostIt.HIDDEN_POST_TEXT : deleted ? PostIt.DELETED_POST_TEXT : r.content();
        boolean pinned = r.pinnedUntil() != null && r.pinnedUntil().isAfter(LocalDateTime.now());
        boolean owner = viewerId != null && r.userId() != null && viewerId.equals(r.userId());
        return PostItRspDto.builder()
                .id(r.id())
                .userId(anonymous ? null : r.userId())
                .nickname(resolveNickname(anonymous, r.nickname()))
                .age(anonymous ? null : r.age())
                .location(anonymous ? null : LocationUtil.composeLocation(r.areaCountry(), r.areaCity()))
                .categoryCode(r.categoryCode())
                .content(content)
                .color(r.color())
                .anonymous(anonymous)
                .lightning(r.categoryCode() == PostCategory.LIGHTN)
                .expiresAt(r.expiresAt())
                .pinnedUntil(r.pinnedUntil())
                .pinned(pinned)
                .replyCount(r.replyCount())
                .likeCount(r.likeCount() == null ? 0L : r.likeCount())
                .likedByMe(Boolean.TRUE.equals(r.likedByMe()))
                .hidden(hidden)
                .deleted(deleted)
                .mine(owner)
                .createTime(r.createTime())
                .build();
    }

    // 작성/owner 액션 응답 — userId/nickname 은 익명이어도 노출 (본인 확인용),
    public static PostItRspDto fromOwnerView(PostIt p, long likeCount, boolean likedByMe, String areaCountry, String areaCity) {
        boolean anonymous = Boolean.TRUE.equals(p.getIsAnonymous());
        Long authorId = p.getUser() == null ? null : p.getUser().getId();
        String authorNickname = p.getUser() == null ? null : p.getUser().getNickname();
        Integer authorAge = p.getUser() == null ? null : p.getUser().getAge();
        return PostItRspDto.builder()
                .id(p.getId())
                .userId(authorId)
                .nickname(authorNickname == null ? ANONYMOUS_NICKNAME : authorNickname)
                .age(anonymous ? null : authorAge)
                .location(anonymous ? null : LocationUtil.composeLocation(areaCountry, areaCity))
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
                .color(p.getColor())
                .anonymous(anonymous)
                .lightning(p.isLightning())
                .expiresAt(p.getExpiresAt())
                .pinnedUntil(p.getPinnedUntil())
                .pinned(p.isPinned())
                .replyCount(p.getReplyCount())
                .likeCount(likeCount)
                .likedByMe(likedByMe)
                .hidden(Boolean.TRUE.equals(p.getIsHidden()))
                .deleted(Boolean.TRUE.equals(p.getIsDeleted()))
                .mine(true)
                .createTime(p.getCreateTime())
                .build();
    }

    private static String resolveNickname(boolean anonymous, String rawNickname) {
        if (anonymous) return ANONYMOUS_NICKNAME;
        // 비익명인데 닉네임이 null → 탈퇴 완료(파기) 회원 (users.nickname 익명화됨)
        return DisplayNameUtil.orWithdrawn(rawNickname);
    }
}
