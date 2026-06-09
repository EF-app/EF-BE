package com.nokcha.efbe.domain.admin.match.dto.response;

import com.nokcha.efbe.domain.match.entity.CodeMatchConfig;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 매칭 설정값 1행 — code_match_config row 1:1
@Schema(description = "매칭 설정값 1행")
public record AdminMatchConfigItemRspDto(
        @Schema(description = "설정 키", example = "weight_keyword") String configKey,
        @Schema(description = "값 (스칼라 또는 JSON 문자열)", example = "0.40") String configValue,
        @Schema(description = "INT / DOUBLE / JSON", example = "DOUBLE") String valueType,
        @Schema(description = "설명") String description,
        @Schema(description = "마지막 수정 시각") LocalDateTime updatedAt,
        @Schema(description = "마지막 수정 관리자 식별자") String updatedBy
) {
    public static AdminMatchConfigItemRspDto from(CodeMatchConfig e) {
        return new AdminMatchConfigItemRspDto(
                e.getConfigKey(),
                e.getConfigValue(),
                e.getValueType(),
                e.getDescription(),
                e.getUpdatedAt(),
                e.getUpdatedBy()
        );
    }
}
