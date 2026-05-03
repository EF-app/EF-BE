package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 밸런스 게임 댓글 레포지토리
public interface BalGameCommentRepository extends JpaRepository<BalGameComment, Long> {

    // 메인홈/상세 노출용 최신 댓글 N개 — 특정 게임의 top-level (대댓글/숨김/삭제 제외)
    @Query("select c from BalGameComment c " +
            "where c.game.id = :gameId " +
            "and c.parent is null " +
            "and (c.isHidden = false or c.isHidden is null) " +
            "and (c.isDeleted = false or c.isDeleted is null) " +
            "order by c.createTime desc")
    List<BalGameComment> findRecentTopComments(@Param("gameId") Long gameId,
                                               org.springframework.data.domain.Pageable pageable);

    // 홈 배치용 — 여러 게임의 top-level 댓글을 한 번에. 게임별 limit 은 호출자가 in-memory 적용.
    @Query("select c from BalGameComment c " +
            "where c.game.id in :gameIds " +
            "and c.parent is null " +
            "and (c.isHidden = false or c.isHidden is null) " +
            "and (c.isDeleted = false or c.isDeleted is null) " +
            "order by c.game.id, c.createTime desc")
    List<BalGameComment> findRecentTopCommentsByGameIds(@Param("gameIds") List<Long> gameIds);

    // 특정 게임 댓글 전체 조회 - 신고 누적 숨김 + 내가 신고한 댓글 제외, 오래된 순(맨 아래가 최신)
    @Query("select c from BalGameComment c " +
            "where c.game.id = :gameId " +
            "and (c.isHidden = false or c.isHidden is null) " +
            "and c.id not in (" +
            "  select r.targetId from Report r " +
            "  where r.targetType = com.nokcha.efbe.domain.report.entity.ReportTargetType.BAL_COMMENT " +
            "  and r.reporter.id = :viewerId" +
            ") " +
            "order by c.createTime asc")
    List<BalGameComment> findVisibleCommentsAsc(@Param("gameId") Long gameId,
                                                @Param("viewerId") Long viewerId);

    // 특정 댓글의 자식 대댓글 존재 여부 (삭제 표시 정책 판단용)
    boolean existsByParentId(Long parentId);
}
