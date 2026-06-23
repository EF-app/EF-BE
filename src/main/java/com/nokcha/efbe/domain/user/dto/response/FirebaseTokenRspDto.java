package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Firebase 커스텀 토큰 응답")
public class FirebaseTokenRspDto {

    @Schema(description = "Firebase Auth custom token. 프론트는 signInWithCustomToken에 사용합니다.", example = "eyJhbGciOiJSUzI1NiJ9...")
    private String firebaseToken;
}
