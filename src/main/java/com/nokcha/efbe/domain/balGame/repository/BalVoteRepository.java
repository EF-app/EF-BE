package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 밸런스 게임 투표 기록 레포지토리
public interface BalVoteRepository extends JpaRepository<BalVote, Long> {

    // 게임 + 유저 단건 조회 (중복 투표 검증)
    Optional<BalVote> findByGameIdAndUserId(Long gameId, Long userId);

    // 게임 + 유저 존재 여부 (댓글창 접근 권한 체크)
    boolean existsByGameIdAndUserId(Long gameId, Long userId);

    // 홈 배치용 — 여러 게임에 대한 특정 유저의 투표를 한 번에.
    List<BalVote> findByGameIdInAndUserId(List<Long> gameIds, Long userId);
}
