package com.nokcha.efbe.domain.postIt.repository.projection;    // admin에서 쓸 거면 패키지 이동해야 함.

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// 관리자 전용 포스트잇 목록/상세 row
public record AdminPostItRow(
        Long id,
        Long userId,
        String userUuid,
        String userNickname,
        Integer userAge,
        String areaCountry,
        String areaCity,
        PostCategory categoryCode,
        String content,
        PostItColor color,
        Boolean isAnonymous,
        LocalDateTime expiresAt,
        LocalDateTime pinnedUntil,
        Integer reportCount,
        Integer replyCount,
        Long likeCount,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}
