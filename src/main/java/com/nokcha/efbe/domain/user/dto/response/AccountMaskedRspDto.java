package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계정 정보 마스킹 조회 응답")
public class AccountMaskedRspDto {

    @Schema(description = "마스킹된 로그인 아이디", example = "efus****")
    private String maskedLoginId;

    @Schema(description = "마스킹된 이메일", example = "ab****@g***.com")
    private String maskedEmail;

    @Schema(description = "이메일 등록 여부", example = "true")
    private boolean hasEmail;

    @Schema(description = "보안코드 설정 여부", example = "true")
    private boolean hasScode;
}
