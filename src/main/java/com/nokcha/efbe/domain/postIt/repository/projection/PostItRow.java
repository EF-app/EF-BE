package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// 포스트잇 피드 row
public record PostItRow(
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
        Boolean likedByMe,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
