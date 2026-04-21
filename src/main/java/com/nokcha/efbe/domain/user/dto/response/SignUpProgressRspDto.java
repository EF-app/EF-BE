package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "회원가입 단계 진행 응답")
public class SignUpProgressRspDto {

    @Schema(description = "다음 단계 진행용 회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @Schema(description = "현재 완료된 회원가입 단계", example = "TERMS_AGREED")
    private String step;

    @Schema(description = "회원가입 토큰 만료 시간", example = "2026-04-15T10:00:00")
    private LocalDateTime expiredAt;
}
