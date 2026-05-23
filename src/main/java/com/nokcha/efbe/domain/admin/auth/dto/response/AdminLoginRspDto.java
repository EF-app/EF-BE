package com.nokcha.efbe.domain.admin.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "관리자 로그인 응답")
public class AdminLoginRspDto {

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "로그인 아이디", example = "admin01")
    private String loginId;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String name;
}
