package com.nokcha.efbe.domain.admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 토큰 갱신 요청")
public class AdminRefreshReqDto {

    @NotBlank(message = "refreshToken은 필수입니다.")
    @Schema(description = "Refresh Token")
    private String refreshToken;
}
