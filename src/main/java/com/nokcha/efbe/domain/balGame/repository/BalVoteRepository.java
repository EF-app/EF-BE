package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalVote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BalVoteRepository extends JpaRepository<BalVote, Long>, BalVoteQueryRepository {

    // 게임 + 유저 단건 조회 (중복 투표 검증)
    Optional<BalVote> findByGameIdAndUserId(Long gameId, Long userId);

    // 게임 + 유저 존재 여부 (댓글창 접근 권한 체크)
    boolean existsByGameIdAndUserId(Long gameId, Long userId);

    // 홈 배치용 — 여러 게임에 대한 특정 유저의 투표를 한 번에.
    List<BalVote> findByGameIdInAndUserId(List<Long> gameIds, Long userId);

    // 어드민 댓글 enrich — 한 게임의 댓글 작성자들이 이 게임에 한 투표를 한 번에 batch.
    List<BalVote> findByGameIdAndUserIdIn(Long gameId, java.util.Collection<Long> userIds);
}
