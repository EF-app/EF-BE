package com.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "회원가입 프로필 등록 응답")
public class SignUpProfileRspDto {

    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @Schema(description = "현재 완료된 회원가입 단계", example = "PROFILE_COMPLETED")
    private String step;

    @ArraySchema(schema = @Schema(description = "업로드된 프로필 이미지 URL"))
    private List<String> imageUrls;
}
