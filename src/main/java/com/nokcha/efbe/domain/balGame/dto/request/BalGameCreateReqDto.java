package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 밸런스 게임 생성 요청 DTO (관리자용)
@Getter
@NoArgsConstructor
public class BalGameCreateReqDto {

    @NotBlank
    @Size(max = 255)
    private String optionA;

    @NotBlank
    @Size(max = 255)
    private String optionB;

    @Size(max = 500)
    private String optionADesc;

    @Size(max = 500)
    private String optionBDesc;

    // 옵션 A/B 표시용 이모지 (선택)
    @Size(max = 8)
    private String optionAEmoji;

    @Size(max = 8)
    private String optionBEmoji;

    private String description;

    @NotNull
    private BalCategoryCode categoryCode;

    private BalGameStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime scheduledEndAt;
}
