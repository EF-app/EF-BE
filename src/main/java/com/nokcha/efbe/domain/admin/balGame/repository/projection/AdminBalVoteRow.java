package com.nokcha.efbe.domain.admin.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;

import java.time.LocalDateTime;

// 관리자 측 밸런스 게임 개별 투표자 row
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
