package com.nokcha.efbe.domain.user.dto.response;

import com.nokcha.efbe.domain.suspension.dto.response.UserSuspensionRspDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "로그인 응답")
public class LoginRspDto {

    @Schema(description = "유저 ID (DB PK, bigint)", example = "1")
    private Long userId;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "로그인 아이디", example = "test001")
    private String loginId;

    @Schema(description = "현재 서버에 저장된 FCM registration token. 없으면 null", example = "fcm_registration_token", nullable = true)
    private String fcmToken;

    @Schema(description = "활성 제재 정보. 없으면 active=false")
    private UserSuspensionRspDto suspension;
}
