package com.nokcha.efbe.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 내가 누른 좋아요 1행. */
@Getter
@Builder
@AllArgsConstructor
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "내가 누른 좋아요 1행")
public class SentLikeItemRspDto {

    @Schema(description = "match_actions.id")
    private String requestId;

    @Schema(description = "create_time ISO")
    private String createdAt;

    @JsonProperty("isSuper")
    @Schema(description = "SUPER_LIKE AND create_time >= NOW-3일")
    private boolean isSuper;

    @Schema(description = "블러 여부 — 기본 등급이 2시간 초과 항목을 볼 때 true. 이때 toUser 는 마스킹됨(프리미엄은 항상 false)")
    private boolean blurred;

    @Schema(description = "받은 사람 정보. blurred=true 면 식별 정보가 마스킹(null)됨")
    private MatchLikeUserDto toUser;
}
