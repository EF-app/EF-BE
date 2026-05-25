package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// 내가 반응한 포스트잇 row
public record UserActivityReactedPostItRow(
        Long id,
        Long userId,
        String nickname,
        Integer age,
        String areaCountry,
        String areaCity,
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
        Boolean chattedByMe,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
