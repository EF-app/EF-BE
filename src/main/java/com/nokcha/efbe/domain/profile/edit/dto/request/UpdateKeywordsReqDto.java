package com.nokcha.efbe.domain.profile.edit.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "관심사 키워드(추천 + 자유 입력) 전체 교체 요청")
public class UpdateKeywordsReqDto {

    @Schema(description = "추천 키워드 ID 목록. 빈 배열이면 모두 제거", example = "[3, 7, 12]")
    private List<Long> keywordIds;

    @Schema(description = "나만의 태그(자유 입력) 목록. 빈 배열이면 모두 제거", example = "[\"퇴근후한잔\", \"러닝메이트\"]")
    private List<String> customKeywords;
}
