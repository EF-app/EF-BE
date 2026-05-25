package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// 내가 붙인 포스트잇 카드 row
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
