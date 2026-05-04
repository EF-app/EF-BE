package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 밸런스 게임 신청 요청 DTO
@Getter
@NoArgsConstructor
public class BalApplyCreateReqDto {

    @NotBlank
    @Size(max = 255)
    private String optionA;

    @NotBlank
    @Size(max = 255)
    private String optionB;

    // 옵션 A/B 표시용 이모지 (선택). VARCHAR(8) — 단일 이모지 + ZWJ 시퀀스까지 안전 커버.
    @Size(max = 8)
    private String optionAEmoji;

    @Size(max = 8)
    private String optionBEmoji;

    private String description;

    @NotNull
    private BalCategoryCode categoryCode;
}
