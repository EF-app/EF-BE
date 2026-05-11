package com.nokcha.efbe.domain.user.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "보안코드 검증 요청")
public class ScodeVerifyReqDto {

    @NotBlank(message = "보안코드는 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "검증할 보안코드", example = "1234")
    private String scode;
}
