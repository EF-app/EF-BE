package com.nokcha.efbe.domain.profile.edit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "기본 정보(닉네임/지역) 수정 요청. 닉네임은 마지막 변경 7일 후부터 가능")
public class UpdateBasicReqDto {

    @Schema(description = "변경할 닉네임. 미변경이면 null", example = "민들레", nullable = true)
    private String nickname;

    @Schema(description = "변경할 지역 PK. 미변경이면 null", example = "12", nullable = true)
    private Long areaId;
}
