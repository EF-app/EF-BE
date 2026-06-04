package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "FCM 토큰 등록/갱신 요청")
public class FcmTokenReqDto {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Size(max = 1024, message = "FCM 토큰은 1024자 이하여야 합니다.")
    @Schema(description = "Firebase SDK에서 발급받은 FCM registration token", example = "fcm_registration_token")
    private String fcmToken;
}
