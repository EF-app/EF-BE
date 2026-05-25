package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalApplyRepository extends JpaRepository<BalApply, Long> {

    // 상태별 최신순 신청 목록 (user fetch 로 N+1 제거)
    @EntityGraph(attributePaths = {"user"})
    Page<BalApply> findByStatusOrderByCreateTimeDesc(BalApplyStatus status, Pageable pageable);
}
