package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;
import com.nokcha.efbe.domain.postIt.entity.PostItColor;

import java.time.LocalDateTime;

// 어드민 포스트잇 목록/상세 projection.
// 익명 마스킹 없음 — 어드민용이라 작성자 정보 원본 그대로 운반.
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
