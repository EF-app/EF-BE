package com.nokcha.efbe.domain.admin.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;

import java.time.LocalDateTime;

// 어드민 측 BalVote 개별 투표자 projection.
public record AdminBalVoteRow(
        Long id,
        Long userId,
        String userUuid,
        String userNickname,
        Integer userAge,
        String areaCountry,
        String areaCity,
        BalVoteChoice choice,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {}
