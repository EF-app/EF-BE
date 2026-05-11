package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그아웃 요청 — refresh 토큰 폐기 (멱등성 보장: 검증 실패해도 200 OK)")
public class LogoutReqDto {

    @Schema(description = "폐기할 refresh 토큰", example = "eyJhbGciOiJI...")
    @NotBlank
    private String refreshToken;
}
