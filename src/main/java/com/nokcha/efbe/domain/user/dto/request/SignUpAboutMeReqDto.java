package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.profile.entity.Mbti;
import com.nokcha.efbe.domain.user.entity.Job;
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
@Schema(description = "회원가입 나에 대해서 입력 요청")
public class SignUpAboutMeReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "회원가입 진행 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @Schema(description = "직업", example = "OFFICE_WORKER")
    private Job job;

    @Schema(description = "MBTI", example = "ENFP")
    private Mbti mbti;

    @ArraySchema(schema = @Schema(description = "나에 대한 성향 ID", example = "27"))
    private List<Long> personalIds;
}
