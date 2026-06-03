package com.nokcha.efbe.domain.profile.edit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "자기소개(bio_message) 수정 요청")
public class UpdateBioReqDto {

    @Size(max = 300)
    @Schema(description = "자기소개 메시지 (최대 300자). null 또는 빈 문자열이면 비움",
            example = "차 한 잔의 여유를 좋아하는 사람입니다.", nullable = true)
    private String bioMessage;
}
