package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.profile.entity.IdealPointType;
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
@Schema(description = "회원가입 이상형 입력 요청")
public class SignUpIdealReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @ArraySchema(schema = @Schema(description = "이상형 성향 ID", example = "27"))
    private List<Long> idealPersonalIds;

    @ArraySchema(schema = @Schema(description = "이상형 포인트", example = "INTEREST"))
    private List<IdealPointType> idealPointTypes;
}
