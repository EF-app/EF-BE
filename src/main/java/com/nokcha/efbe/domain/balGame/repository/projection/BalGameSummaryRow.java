package com.nokcha.efbe.domain.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;

import java.time.LocalDateTime;

// 밸런스 게임 피드 카드 projection
public record BalGameSummaryRow(
        Long id,
        String optionA,
        String optionB,
        String optionAEmoji,
        String optionBEmoji,
        BalCategoryCode categoryCode,
        BalGameStatus status,
        Integer totalCount,
        Integer aCount,
        Integer bCount,
        Integer commentCount,
        LocalDateTime scheduledAt,
        LocalDateTime createTime
) {}
