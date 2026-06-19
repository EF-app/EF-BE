package com.nokcha.efbe.domain.match.dto.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 받은 좋아요 1행. */
@Getter
@Builder
@AllArgsConstructor
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Schema(description = "받은 좋아요 1행")
public class ReceivedLikeItemRspDto {

    @Schema(description = "match_actions.id")
    private String requestId;

    @Schema(description = "create_time ISO")
    private String createdAt;

    @JsonProperty("isSuper")
    @Schema(description = "SUPER_LIKE AND create_time >= NOW-3일")
    private boolean isSuper;

    @Schema(description = "보낸 사람 정보")
    private MatchLikeUserDto fromUser;
}
