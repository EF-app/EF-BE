package com.nokcha.efbe.domain.admin.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;

// 관리자 측 투표 통계 bucket row
public record AdminBalVoteBucketRow(
        String bucketLabel,
        BalVoteChoice choice,
        long count
) {}
