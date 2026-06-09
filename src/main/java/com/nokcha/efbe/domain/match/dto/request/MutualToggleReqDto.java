package com.nokcha.efbe.domain.match.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** mutual 토글 요청 body */
@Schema(description = "서로 좋아요 카드 하트 토글 — CANCEL=매칭 끊기, RESTORE=복구")
public record MutualToggleReqDto(
        @NotNull
        @Schema(description = "CANCEL | RESTORE", example = "CANCEL")
        Action action
) {
    public enum Action { CANCEL, RESTORE }
}
