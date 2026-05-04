package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;

import java.time.LocalDateTime;

// 포스트잇 피드 projection (PostIt 단일 테이블 — category_code 가 인라인 ENUM 으로 변경됨)
// content/anonymous 는 표시 정책을 서비스 단에서 적용하므로 raw 값 그대로 운반
public record PostItRow(
        Long id,
        Long userId,
        PostCategory categoryCode,
        String content,
        Boolean isAnonymous,
        LocalDateTime expiresAt,
        LocalDateTime pinnedUntil,
        Integer replyCount,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
