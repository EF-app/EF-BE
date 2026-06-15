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
        @Schema(description = "마지막 수정 시각") LocalDateTime updateTime,
        @Schema(description = "마지막 수정 사용자 id (0 = 시스템)") Long updateUser
) {
    public static AdminMatchConfigItemRspDto from(CodeMatchConfig e) {
        return new AdminMatchConfigItemRspDto(
                e.getConfigKey(),
                e.getConfigValue(),
                e.getValueType(),
                e.getDescription(),
                e.getUpdateTime(),
                e.getUpdateUser()
        );
    }
}
