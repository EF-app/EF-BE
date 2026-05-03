package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "회원가입 완료 응답")
public class SignUpCompleteRspDto {

    @Schema(description = "생성된 회원 ID", example = "1")
    private Long userId;

    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "로그인 아이디", example = "efuser01")
    private String loginId;

    @Schema(description = "현재 완료된 회원가입 단계", example = "SIGNUP_COMPLETED")
    private String step;

    @Schema(description = "회원가입 완료 여부", example = "true")
    private boolean completed;
}
