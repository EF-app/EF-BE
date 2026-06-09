package com.nokcha.efbe.domain.admin.match.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

// 매칭 설정값 부분 갱신 요청 — 변경된 row 만 entries 로 전달
@Schema(description = "매칭 설정값 부분 갱신 요청")
public record AdminMatchConfigUpdateReqDto(
        @Schema(description = "변경 대상 entries (변경된 row 만)")
        @NotEmpty(message = "변경할 항목이 비어있습니다.")
        @Valid List<Entry> entries
) {
    @Schema(description = "key/value 1쌍 — value 는 valueType 에 맞는 표현 (INT/DOUBLE 은 숫자 문자열, JSON 은 JSON 문자열)")
    public record Entry(
            @Schema(description = "설정 키", example = "weight_keyword")
            @NotBlank(message = "configKey 는 필수입니다.")
            String configKey,

            @Schema(description = "값", example = "0.45")
            @NotNull(message = "configValue 는 필수입니다.")
            String configValue
    ) {}
}
