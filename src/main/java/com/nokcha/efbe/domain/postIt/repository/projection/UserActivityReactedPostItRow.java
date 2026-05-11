package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;

import java.time.LocalDateTime;

// "내가 반응한 포스트잇" projection — 상대 글에 좋아요/채팅 반응
// - 정렬은 post_it.create_time DESC + post_it.id DESC (상대 글 작성 시각 기준)
// - likedByMe / chattedByMe 는 서브쿼리 exists
// - likeCount = post_like 전체, chatCount = post_chat_room 전체 (active/closed 무관)
public record UserActivityReactedPostItRow(
        Long id,
        Long userId,
        String nickname,
        Integer age,
        String areaCountry,
        String areaCity,
        PostCategory categoryCode,
        String content,
        Boolean isAnonymous,
        LocalDateTime expiresAt,
        LocalDateTime pinnedUntil,
        Integer replyCount,
        Long likeCount,
        Long chatCount,
        Boolean likedByMe,
        Boolean chattedByMe,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
