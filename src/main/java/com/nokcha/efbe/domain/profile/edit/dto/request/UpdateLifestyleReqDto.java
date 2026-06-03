package com.nokcha.efbe.domain.profile.edit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "생활 습관(음주/주종/흡연/타투) 교체 요청. " +
        "code_personal.big_category in ('음주','선호 주종','흡연','흡연 종류','타투유무') 의 personalId 만 허용")
public class UpdateLifestyleReqDto {

    @Schema(description = "선택한 personalId 목록. 해당 카테고리 SELF row 전체 교체",
            example = "[51, 56, 60, 67]")
    private List<Long> personalIds;
}
