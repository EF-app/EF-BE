package com.nokcha.efbe.domain.admin.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "관리자 토큰 갱신 응답")
public record AdminTokenRspDto(
        @Schema(description = "재발급된 액세스 토큰") String accessToken,
        @Schema(description = "기존 리프레시 토큰 (Phase 1 은 재사용, Phase 2 에서 rotation)") String refreshToken
) {
}
