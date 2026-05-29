package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;
import com.nokcha.efbe.domain.postIt.repository.projection.UserActivityPostItRow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "내 활동 — 내가 붙인 포스트잇 카드")
public class UserActivityPostItRspDto {

    public static final String ANONYMOUS_NICKNAME = "익명";

    @Schema(description = "포스트잇 PK", example = "1")
    private Long id;

    @Schema(description = "작성자 user_id (본인)", example = "10")
    private Long userId;

    @Schema(description = "표시용 닉네임 — 익명 글이면 '익명'", example = "이프차")
    private String nickname;

    @Schema(description = "나이 (한국 나이). 익명 글이면 null.", example = "27")
    private Integer age;

    @Schema(description = "지역 'country city'. 익명 글이면 null.", example = "서울특별시 강남구")
    private String location;

    @Schema(description = "카테고리", example = "DAILY")
    private PostCategory categoryCode;

    @Schema(description = "본문 (숨김/삭제 정책 치환 적용)")
    private String content;

    @Schema(description = "포스트잇 색상 슬롯 (P1~P5). FE 가 슬롯별 hex 매핑.", example = "P1")
    private PostItColor color;

    @Schema(description = "익명 여부", example = "false")
    private Boolean isAnonymous;

    @Schema(description = "번개 카테고리 여부", example = "false")
    private Boolean isLightning;

    @Schema(description = "만료 시각")
    private LocalDateTime expiresAt;

    @Schema(description = "상단 고정 만료 시각 (null 이면 미고정)")
    private LocalDateTime pinnedUntil;

    @Schema(description = "현재 상단 고정 활성 여부", example = "false")
    private Boolean isPinned;

    @Schema(description = "답장 수", example = "3")
    private Integer replyCount;

    @Schema(description = "좋아요 수", example = "12")
    private long likeCount;

    @Schema(description = "채팅방 수 (active/closed 무관 누적)", example = "4")
    private long chatCount;

    @Schema(description = "내가 좋아요 눌렀는지", example = "false")
    private boolean likedByMe;

    @Schema(description = "숨김 여부", example = "false")
    private Boolean isHidden;

    @Schema(description = "삭제 여부 (soft delete)", example = "false")
    private boolean deleted;

    @Schema(description = "작성 시각")
    private LocalDateTime createTime;

    // Querydsl projection + 본인 area 캐시 기반 매핑
    public static UserActivityPostItRspDto from(UserActivityPostItRow r, String ownerNickname, Integer ownerAge, String ownerAreaCountry, String ownerAreaCity) {
        boolean anonymous = Boolean.TRUE.equals(r.isAnonymous());
        boolean hidden = Boolean.TRUE.equals(r.isHidden());
        boolean deleted = Boolean.TRUE.equals(r.isDeleted());
        String content = hidden ? PostIt.HIDDEN_POST_TEXT : deleted ? PostIt.DELETED_POST_TEXT : r.content();
        boolean pinned = r.pinnedUntil() != null && r.pinnedUntil().isAfter(LocalDateTime.now());

        return UserActivityPostItRspDto.builder()
                .id(r.id())
                .userId(r.userId())
                .nickname(anonymous ? ANONYMOUS_NICKNAME : (ownerNickname == null ? ANONYMOUS_NICKNAME : ownerNickname))
                .age(anonymous ? null : ownerAge)
                .location(anonymous ? null : LocationUtil.composeLocation(ownerAreaCountry, ownerAreaCity))
                .categoryCode(r.categoryCode())
                .content(content)
                .color(r.color())
                .isAnonymous(anonymous)
                .isLightning(r.categoryCode() == PostCategory.LIGHTN)
                .expiresAt(r.expiresAt())
                .pinnedUntil(r.pinnedUntil())
                .isPinned(pinned)
                .replyCount(r.replyCount())
                .likeCount(r.likeCount() == null ? 0L : r.likeCount())
                .chatCount(r.chatCount() == null ? 0L : r.chatCount())
                .likedByMe(Boolean.TRUE.equals(r.likedByMe()))
                .isHidden(hidden)
                .deleted(deleted)
                .createTime(r.createTime())
                .build();
    }
}
