package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "밸런스 게임 신청 요청")
public class BalApplyCreateReqDto {

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    @NotBlank
    @Size(max = 255)
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    @NotBlank
    @Size(max = 255)
    private String optionB;

    // 옵션 A/B 표시용 이모지 (선택). VARCHAR(8) — 단일 이모지 + ZWJ 시퀀스까지 안전 커버.
    @Schema(description = "옵션 A 표시용 이모지 (선택). 단일 이모지 + ZWJ 시퀀스까지 안전 커버", example = "💳")
    @Size(max = 8)
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지 (선택)", example = "🎧")
    @Size(max = 8)
    private String optionBEmoji;

    @Schema(description = "신청 사유/배경 설명")
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    @NotNull
    private BalCategoryCode categoryCode;
}
