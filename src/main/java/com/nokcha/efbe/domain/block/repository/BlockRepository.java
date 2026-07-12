package com.nokcha.efbe.domain.block.repository;

import com.nokcha.efbe.domain.block.entity.Block;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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
    @Query("select b.blocked.id from Block b where b.blocker.id = :blockerId")
    List<Long> findBlockedUserIds(@Param("blockerId") Long blockerId);

    // userId 와 차단 관계(양방향)인 상대 유저 id 전체 — 채팅방 일괄 활성화 판정용(N+1 방지)
    @Query("select case when b.blocker.id = :userId then b.blocked.id else b.blocker.id end " +
            "from Block b where b.blocker.id = :userId or b.blocked.id = :userId")
    List<Long> findCounterpartUserIds(@Param("userId") Long userId);

    // 어드민 차단 내역 — keyword(차단자/피차단자 닉네임·UUID LIKE) 동적 필터
    @Query("select b from Block b " +
            "where (:keyword is null " +
            "       or b.blocker.nickname like concat('%', :keyword, '%') " +
            "       or b.blocker.uuid like concat('%', :keyword, '%') " +
            "       or b.blocked.nickname like concat('%', :keyword, '%') " +
            "       or b.blocked.uuid like concat('%', :keyword, '%'))")
    Page<Block> searchForAdmin(@Param("keyword") String keyword,
                               Pageable pageable);

    // 어드민 차단 내역 — 한 페이지 유저들 사이의 모든 차단 쌍 (상호 차단 isMutual 판정용)
    List<Block> findByBlocker_IdInAndBlocked_IdIn(Collection<Long> blockerIds, Collection<Long> blockedIds);
}
