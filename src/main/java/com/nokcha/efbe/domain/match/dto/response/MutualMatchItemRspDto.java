package com.nokcha.efbe.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 서로 좋아요 1행.
 *  isSuper : 양쪽 중 하나라도 SUPER_LIKE 였는지 (mutual 차등 표시용)
 *  isFresh : matchedAt >= NOW - 3시간 (fresh strip 노출 대상)
 *
 *  ※ isLiked 필드는 BE 응답에서 제거됨 — searchMutual SQL 의 WHERE 절은 내 LIKE/SUPER_LIKE EXISTS 를 강제하므로
 *    BE 가 응답한 row 는 항상 isLiked=true 와 동치이며, PASS 토글된 페어는 SQL 결과에서 자동 제외된다.
 *    FE 는 카드 토글 (CANCEL/RESTORE) 옵티미스틱 상태를 자체적으로 관리하고, 다음 refetch 시 SQL 이 PASS 페어를 빼며 자연 정합.
 */
@Getter
@Builder
@AllArgsConstructor
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "서로 좋아요 1행")
public class MutualMatchItemRspDto {

    @Schema(description = "페어 식별자 — match_results.id")
    private String matchId;

    @Schema(description = "매칭 성사 시점 ISO — match_results.create_time")
    private String matchedAt;

    @JsonProperty("isFresh")
    @Schema(description = "방금 매칭 — matchedAt >= NOW-3h")
    private boolean isFresh;

    @JsonProperty("isSuper")
    @Schema(description = "슈퍼 매칭 — 양쪽 중 하나라도 SUPER_LIKE")
    private boolean isSuper;

    @Schema(description = "채팅방 id (v1 미구현 — null)")
    private String chatRoomId;

    @Schema(description = "상대 정보")
    private MatchLikeUserDto user;
}
