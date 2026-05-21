package com.nokcha.efbe.domain.block.repository;

import com.nokcha.efbe.domain.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    // 이미 차단했는지 — (blocker, blocked) 쌍 중복 방지
    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    // 차단 해제용 — (blocker, blocked) 쌍으로 차단 레코드 조회
    Optional<Block> findByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);

    // 내 차단 목록 — 최신순
    List<Block> findByBlocker_IdOrderByCreateTimeDesc(Long blockerId);

    // 내가 차단한 유저들의 id — 포스트잇 등 콘텐츠 필터링용
    @org.springframework.data.jpa.repository.Query(
            "select b.blocked.id from Block b where b.blocker.id = :blockerId")
    List<Long> findBlockedUserIds(@org.springframework.data.repository.query.Param("blockerId") Long blockerId);
}
