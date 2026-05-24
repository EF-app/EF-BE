package com.nokcha.efbe.domain.admin.balGame.repository.projection;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;

// 어드민 측 투표 통계 bucket projection.
public record AdminBalVoteBucketRow(
        String bucketLabel,
        BalVoteChoice choice,
        long count
) {}
