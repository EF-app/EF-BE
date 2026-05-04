package com.nokcha.efbe.domain.postIt.repository.projection;

import com.nokcha.efbe.domain.postIt.entity.PostCategory;

import java.time.LocalDateTime;

// 포스트잇 피드 projection (PostIt 단일 테이블 — category_code 가 인라인 ENUM 으로 변경됨)
// content/anonymous 는 표시 정책을 서비스 단에서 적용하므로 raw 값 그대로 운반
// nickname/age/areaCountry/areaCity 는 익명 마스킹 적용 전 raw 값 (DTO 단계에서 anonymous 면 마스킹/null)
//   - age 는 users.age 컬럼 (한국 나이, 휴대폰 인증 단계에서 산출·저장. 빠른년생 수정 가능)
// likeCount/likedByMe 는 피드 쿼리에서 집계·서브쿼리로 산출
public record PostItRow(
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
        Boolean likedByMe,
        Boolean isHidden,
        Boolean isDeleted,
        LocalDateTime createTime
) {}
