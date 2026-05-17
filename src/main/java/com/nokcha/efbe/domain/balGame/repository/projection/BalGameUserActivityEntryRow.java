package com.nokcha.efbe.domain.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;

import java.time.LocalDateTime;

// "내가 투표한 밸런스게임" projection — bal_vote JOIN bal_game
public record BalGameUserActivityEntryRow(
        Long gameId,
        String optionA,
        String optionB,
        String optionAEmoji,
        String optionBEmoji,
        BalCategoryCode categoryCode,
        BalGameStatus status,
        Integer aCount,
        Integer bCount,
        Integer commentCount,
        BalVoteChoice myChoice,
        LocalDateTime myVotedAt,
        Long voteId,
        LocalDateTime gameCreateTime
) {}
