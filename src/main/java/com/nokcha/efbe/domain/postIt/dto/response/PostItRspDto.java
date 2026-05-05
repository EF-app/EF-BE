package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.projection.PostItRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 응답 DTO (삭제/숨김 표시 정책 + 익명 처리 반영)
// 익명 마스킹 정책:
//   - 모든 viewing(피드/내가 쓴 글/단건 조회) 응답에서는 익명 글이면 userId=null, nickname="익명", age/location=null
//   - 본인 시점도 동일 — "메인 피드에서 본인 글이라도 닉네임 노출 금지" 요구사항 충족
//   - 단, 작성/owner 액션 응답(createPostIt, activatePin) 만 fromOwnerView 로 userId/nickname 노출 (본인 확인용)
//     - age/location 은 anonymous=true 면 owner view 에서도 노출하지 않음 ("익명이니 안 나옴" 정책 일관 적용)
//   - 신고 처리 등 admin 흐름은 별도 admin DTO 사용 (본 DTO 와 무관)
// 좋아요 필드:
//   - likeCount: 해당 포스트의 누적 좋아요 수
//   - likedByMe: viewer(현재 로그인 유저)가 좋아요를 눌렀는지. 비로그인 시 false.
// age 필드:
//   - users.age 컬럼 값을 그대로 노출 (한국 나이). 휴대폰 인증 시 산출·저장, 빠른년생 수정 가능.
//   - 미설정(null) 사용자는 age=null 그대로 전달.
@Getter
@Builder
public class PostItRspDto {
    public static final String ANONYMOUS_NICKNAME = "익명";

    private Long id;
    private Long userId;         // 익명이면 null (단 fromOwnerView 경로는 노출)
    private String nickname;     // 익명이면 "익명", 일반 글은 작성자 nickname
    private Integer age;         // 익명이면 null. 일반 글은 users.age (한국 나이).
    private String location;     // 익명이면 null. 일반 글은 "country city" (예: "서울특별시 강남구")
    private PostCategory categoryCode;
    private String content;
    private boolean anonymous;
    private boolean lightning;
    private LocalDateTime expiresAt;
    private LocalDateTime pinnedUntil;
    private boolean pinned;
    private Integer replyCount;
    private long likeCount;
    private boolean likedByMe;
    private boolean hidden;
    private boolean deleted;
    private LocalDateTime createTime;

    // viewing 기본값 — 익명이면 userId/nickname/age/location 마스킹
    public static PostItRspDto from(PostIt p, long likeCount, boolean likedByMe, String areaCountry, String areaCity) {
        boolean anonymous = Boolean.TRUE.equals(p.getIsAnonymous());
        Long authorId = p.getUser() == null ? null : p.getUser().getId();
        String authorNickname = p.getUser() == null ? null : p.getUser().getNickname();
        Integer authorAge = p.getUser() == null ? null : p.getUser().getAge();
        return PostItRspDto.builder()
                .id(p.getId())
                .userId(anonymous ? null : authorId)
                .nickname(resolveNickname(anonymous, authorNickname))
                .age(anonymous ? null : authorAge)
                .location(anonymous ? null : composeLocation(areaCountry, areaCity))
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
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
                .createTime(p.getCreateTime())
                .build();
    }

    // Querydsl projection 기반 — 신규 피드 표준
    // 표시 정책 (삭제/숨김 치환) 을 row 단계에서 적용. 익명이면 userId/nickname/age/location 마스킹.
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
                .nickname(resolveNickname(anonymous, r.nickname()))
                .age(anonymous ? null : r.age())
                .location(anonymous ? null : composeLocation(r.areaCountry(), r.areaCity()))
                .categoryCode(r.categoryCode())
                .content(content)
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
                .createTime(r.createTime())
                .build();
    }

    // 작성/owner 액션 응답 — userId/nickname 은 익명이어도 노출 (본인 확인용),
    // 단 age/location 은 anonymous=true 면 노출하지 않음 (익명 정책 일관).
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
                .location(anonymous ? null : composeLocation(areaCountry, areaCity))
                .categoryCode(p.getCategoryCode())
                .content(p.resolveDisplayContent())
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
                .createTime(p.getCreateTime())
                .build();
    }

    private static String resolveNickname(boolean anonymous, String rawNickname) {
        if (anonymous) return ANONYMOUS_NICKNAME;
        return rawNickname == null ? ANONYMOUS_NICKNAME : rawNickname;
    }

    // country+city → "country city". 둘 다 null 이면 null, 한쪽만 있으면 그 값만.
    private static String composeLocation(String country, String city) {
        boolean hasCountry = country != null && !country.isBlank();
        boolean hasCity = city != null && !city.isBlank();
        if (!hasCountry && !hasCity) return null;
        if (hasCountry && hasCity) return country + " " + city;
        return hasCountry ? country : city;
    }
}
