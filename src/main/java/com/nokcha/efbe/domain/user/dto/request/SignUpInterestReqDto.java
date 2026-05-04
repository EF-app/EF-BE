package com.nokcha.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 관심사 입력 요청")
public class SignUpInterestReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @ArraySchema(schema = @Schema(description = "관심사 ID", example = "1"))
    private List<Long> interestIds;

    @ArraySchema(schema = @Schema(description = "나만의 관심사 키워드", example = "LP바"))
    private List<String> customKeywords;
}
