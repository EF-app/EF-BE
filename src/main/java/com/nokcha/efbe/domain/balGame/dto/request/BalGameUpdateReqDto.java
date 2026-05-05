package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 밸런스 게임 수정 요청 DTO (관리자용)
@Getter
@NoArgsConstructor
public class BalGameUpdateReqDto {

    private String optionA;
    private String optionB;
    private String optionADesc;
    private String optionBDesc;
    private String optionAEmoji;
    private String optionBEmoji;
    private String description;
    private BalCategoryCode categoryCode;
    private BalGameStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime scheduledEndAt;
}
