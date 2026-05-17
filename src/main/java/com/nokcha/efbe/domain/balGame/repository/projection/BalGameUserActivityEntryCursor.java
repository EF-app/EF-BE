package com.nokcha.efbe.domain.balGame.repository.projection;

import java.time.LocalDateTime;

// "내가 투표한 밸런스게임" 목록 커서 (myVotedAt DESC, voteId DESC 정렬용)
public record BalGameUserActivityEntryCursor(LocalDateTime votedAt, Long voteId) {}
