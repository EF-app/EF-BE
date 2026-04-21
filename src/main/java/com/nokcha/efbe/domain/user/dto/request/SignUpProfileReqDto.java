package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.profile.entity.Mbti;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 프로필 등록 요청")
public class SignUpProfileReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @Schema(description = "MBTI", example = "ENFP")
    private Mbti mbti;

    @NotBlank(message = "한 줄 소개는 필수입니다.")
    @Size(max = 255, message = "한 줄 소개는 255자 이하로 입력해야 합니다.")
    @Schema(description = "한 줄 소개", example = "술은 가끔, 담배는 안 하고 대화가 잘 통하는 사람이 좋아요.")
    private String message;

    @ArraySchema(schema = @Schema(description = "관심사 ID", example = "1"))
    private List<Long> interestIds;

    @ArraySchema(schema = @Schema(description = "나만의 관심사 키워드", example = "LP바"))
    private List<String> customKeywords;

    @ArraySchema(schema = @Schema(description = "내 성향 ID", example = "1"))
    private List<Long> personalIds;

    @ArraySchema(schema = @Schema(description = "이상형 성향 ID", example = "27"))
    private List<Long> idealPersonalIds;
}
