package com.nokcha.efbe.domain.profile.dto.request;

import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "이상형(끌리는 스타일 + 중요 포인트) 교체 요청. " +
        "personalIds 는 type=IDEAL 로 저장. " +
        "code_personal.big_category in ('머리','체형','키','성향') 만 허용")
public class UpdateIdealReqDto {

    @Schema(description = "이상형 personalId 목록 (type=IDEAL)", example = "[110, 117, 124]")
    private List<Long> personalIds;

    @Schema(description = "이상형 중요 포인트 (user_profile.ideal_point_types)", example = "[\"PERSONALITY\", \"VALUES\"]")
    private List<IdealPointType> idealPointTypes;
}
