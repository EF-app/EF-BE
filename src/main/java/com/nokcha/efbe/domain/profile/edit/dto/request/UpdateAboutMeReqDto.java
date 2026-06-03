package com.nokcha.efbe.domain.profile.edit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "나에 대해(일상/종교/이쪽 지인/커밍아웃) 교체 요청. " +
        "code_personal.big_category in ('일상 유형','종교','이쪽 지인','커밍아웃 정도') 의 personalId 만 허용")
public class UpdateAboutMeReqDto {

    @Schema(description = "선택한 personalId 목록. 해당 카테고리 SELF row 전체 교체",
            example = "[81, 88, 95, 102]")
    private List<Long> personalIds;
}
