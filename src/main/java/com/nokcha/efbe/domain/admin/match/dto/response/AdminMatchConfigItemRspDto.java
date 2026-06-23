package com.nokcha.efbe.domain.admin.match.dto.response;

import com.nokcha.efbe.domain.match.entity.CodeMatchConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 매칭 설정값 1행 — code_match_config row 1:1
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "매칭 설정값 1행")
public class AdminMatchConfigItemRspDto {

    @Schema(description = "설정 키", example = "weight_keyword")
    private String configKey;

    @Schema(description = "값 (스칼라 또는 JSON 문자열)", example = "0.40")
    private String configValue;

    @Schema(description = "INT / DOUBLE / JSON", example = "DOUBLE")
    private String valueType;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "마지막 수정 시각")
    private LocalDateTime updateTime;

    @Schema(description = "마지막 수정 사용자 id (0 = 시스템)")
    private Long updateUser;

    public static AdminMatchConfigItemRspDto from(CodeMatchConfig e) {
        return AdminMatchConfigItemRspDto.builder()
                .configKey(e.getConfigKey())
                .configValue(e.getConfigValue())
                .valueType(e.getValueType())
                .description(e.getDescription())
                .updateTime(e.getUpdateTime())
                .updateUser(e.getUpdateUser())
                .build();
    }
}
