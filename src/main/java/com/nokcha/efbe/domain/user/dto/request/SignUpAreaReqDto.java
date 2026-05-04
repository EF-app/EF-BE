package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 지역 입력 요청")
public class SignUpAreaReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @NotNull(message = "지역 ID는 필수입니다.")
    @Schema(description = "지역 ID(code_area.id)", example = "1")
    private Long areaId;
}
