package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryCursor;
import com.nokcha.efbe.domain.balGame.repository.projection.BalGameUserActivityEntryRow;

import java.util.List;

// 밸런스 게임 사용자 활동 — Querydsl 인터페이스
// "내가 투표한 게임 목록" 등 bal_vote ↔ bal_game 결합 쿼리 전담
public interface BalVoteQueryRepository {

    // 내가 투표한 게임 목록 (DRAFT/HIDDEN 제외, PUBLISHED + ARCHIVED 노출)
    // 정렬: bal_vote.create_time DESC, bal_vote.id DESC (안정 정렬)
    // size+1 fetch 로 hasMore 판정 가능
    List<BalGameUserActivityEntryRow> findMyVotedGames(Long userId, BalGameUserActivityEntryCursor cursor, int size);
}
