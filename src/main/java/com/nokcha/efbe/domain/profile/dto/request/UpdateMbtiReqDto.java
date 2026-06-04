package com.nokcha.efbe.domain.profile.dto.request;

import com.nokcha.efbe.domain.profile.entity.Mbti;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "MBTI 단독 수정 요청")
public class UpdateMbtiReqDto {

    @Schema(description = "MBTI. null 이면 미설정으로 변경", example = "ENFP", nullable = true)
    private Mbti mbti;
}
