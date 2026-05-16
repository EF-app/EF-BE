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
@Schema(description = "보안코드 변경 요청")
public class ScodeChangeReqDto {

    @NotBlank(message = "기존 보안코드는 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "현재 보안코드", example = "1234")
    private String oldScode;

    @NotBlank(message = "새 보안코드는 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "새 보안코드", example = "5678")
    private String newScode;

    @NotBlank(message = "새 보안코드 확인은 필수입니다.")
    @Pattern(regexp = "\\d{4}", message = "보안코드는 숫자 4자리여야 합니다.")
    @Schema(description = "새 보안코드 확인", example = "5678")
    private String newScodeConfirm;
}
