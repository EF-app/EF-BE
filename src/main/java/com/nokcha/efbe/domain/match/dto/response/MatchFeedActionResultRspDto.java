package com.nokcha.efbe.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 매칭피드 카드 액션 (createAction) 등록 결과.
 *  isMatched 등 boolean 필드의 record 시절 JSON 키 (isXxx) 호환 — Lombok getter 인식 차단 + 필드 직렬화.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "매칭피드 카드 액션 등록 결과")
public class MatchFeedActionResultRspDto {

    //true 면 클라이언트가 매칭 popup 표시 + 서로 좋아요 화면 라우팅 가능.
    @JsonProperty("isMatched")
    @Schema(description = "양방향 LIKE/SUPER_LIKE/POWER_MESSAGE 양방향 매칭 성사 여부")
    private boolean isMatched;

    @Schema(description = "채팅방 id (chat 도메인 작업 후. 현재 null)")
    private String chatRoomId;
}
