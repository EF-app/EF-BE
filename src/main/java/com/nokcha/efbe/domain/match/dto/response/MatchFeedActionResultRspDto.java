package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 매칭피드 카드 액션 (createAction) 등록 결과.
 */
@Schema(description = "매칭피드 카드 액션 등록 결과")
public record MatchFeedActionResultRspDto(
        //true 면 클라이언트가 매칭 popup 표시 + 서로 좋아요 화면 라우팅 가능.
        @Schema(description = "양방향 LIKE/SUPER_LIKE/POWER_MESSAGE 양방향 매칭 성사 여부") boolean isMatched,
        @Schema(description = "채팅방 id (chat 도메인 작업 후. 현재 null)") String chatRoomId
) {}
