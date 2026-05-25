package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalGameComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BalGameCommentRepository extends JpaRepository<BalGameComment, Long> {

    // 메인홈/상세 노출용 최신 댓글 N개 — 특정 게임의 top-level (대댓글/숨김/삭제 제외)
    @Query("select c from BalGameComment c " +
            "where c.game.id = :gameId " +
            "and c.parent is null " +
            "and (c.isHidden = false or c.isHidden is null) " +
            "and (c.isDeleted = false or c.isDeleted is null) " +
            "order by c.createTime desc")
    List<BalGameComment> findRecentTopComments(@Param("gameId") Long gameId, Pageable pageable);

    // 홈 배치용 — 여러 게임의 top-level 댓글을 한 번에. 게임별 limit 은 호출자가 in-memory 적용.
    @Query("select c from BalGameComment c " +
            "where c.game.id in :gameIds " +
            "and c.parent is null " +
            "and (c.isHidden = false or c.isHidden is null) " +
            "and (c.isDeleted = false or c.isDeleted is null) " +
            "order by c.game.id, c.createTime desc")
    List<BalGameComment> findRecentTopCommentsByGameIds(@Param("gameIds") List<Long> gameIds);

    // 특정 게임 댓글 전체 조회 - 신고 누적 숨김 + 내가 신고한 댓글 제외, 오래된 순(맨 아래 최신)
    // TODO(merge-squash): main 에 report 도메인 미존재 — Report 서브쿼리 부분만 주석. report 도메인 합류 후 라인 주석 해제.
    @Query("select c from BalGameComment c " +
            "where c.game.id = :gameId " +
            "and (c.isHidden = false or c.isHidden is null) " +
            // "and c.id not in (" +
            // "  select r.targetId from Report r " +
            // "  where r.targetType = com.nokcha.efbe.domain.report.entity.ReportTargetType.BAL_COMMENT " +
            // "  and r.reporter.id = :viewerId" +
            // ") " +
            "order by c.createTime asc")
    List<BalGameComment> findVisibleCommentsAsc(@Param("gameId") Long gameId, @Param("viewerId") Long viewerId);

    // 어드민 댓글 페이지 — 숨김/삭제 모두 노출 (어드민이 직접 판단).
    Page<BalGameComment> findByGameId(Long gameId, Pageable pageable);

    // 어드민 — 유저 상세 "작성한 댓글" 숨김/삭제 포함.
    Page<BalGameComment> findByUser_IdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}
