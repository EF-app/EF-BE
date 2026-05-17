package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// "내가 붙인 포스트잇" 카드 projection
// - 본인 글이므로 user 정보(nickname/age/area)는 별도 lookup 으로 캐시해서 채움
// - likeCount = post_like 서브쿼리, chatCount = post_chat_room 서브쿼리 (active/closed 무관)
// - likedByMe = post_like 본인 EXISTS (본인이 자기 글에 좋아요 누른 경우 true)
public record UserActivityPostItRow(
        Long id,
        Long userId,
        PostCategory categoryCode,
        String content,
        PostItColor color,
        Boolean isAnonymous,
        LocalDateTime expiresAt,
        LocalDateTime pinnedUntil,
        Integer replyCount,
        Long likeCount,
        Long chatCount,
        Boolean likedByMe,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
