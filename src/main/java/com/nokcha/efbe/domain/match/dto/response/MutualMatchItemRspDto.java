package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 서로 좋아요 1행
 *  isSuper : 양쪽 중 하나라도 SUPER_LIKE 였는지 (mutual 차등 표시용)
 *  isLiked : 내 row 가 LIKE/SUPER_LIKE 상태 (true) 또는 PASS 로 토글된 상태 (false)
 *            UI 의 빨간 채움 하트 vs 빈 outline 토글에 사용
 *  isFresh : matchedAt >= NOW - 3시간 (fresh strip 노출 대상)
 */
@Schema(description = "서로 좋아요 1행")
public record MutualMatchItemRspDto(
        @Schema(description = "페어 식별자 — LEAST(ma1.id, ma2.id)")
        String matchId,
        @Schema(description = "매칭 성사 시점 ISO — GREATEST 양쪽 LIKE create_time")
        String matchedAt,
        @Schema(description = "방금 매칭 — matchedAt >= NOW-3h")
        boolean isFresh,
        @Schema(description = "슈퍼 매칭 — 양쪽 중 하나라도 SUPER_LIKE")
        boolean isSuper,
        @Schema(description = "내 LIKE 상태 (false=내가 cancel 토글)")
        boolean isLiked,
        @Schema(description = "채팅방 id (v1 미구현 — null)")
        String chatRoomId,
        @Schema(description = "상대 정보") MatchLikeUserDto user
) {}
