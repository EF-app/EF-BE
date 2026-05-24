package com.nokcha.efbe.domain.balGame.repository;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BalGameRepository extends JpaRepository<BalGame, Long>, BalGameQueryRepository {

    // 단건 조회 (PESSIMISTIC_WRITE 락 - 카운트 동시성 보호용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from BalGame g where g.id = :id")
    Optional<BalGame> findByIdForUpdate(@Param("id") Long id);

    // 상태별 최신순 페이징
    Page<BalGame> findByStatusOrderByCreateTimeDesc(BalGameStatus status, Pageable pageable);

    // 어드민 측 — status / categoryCode 동적 필터
    @Query(value = "SELECT g FROM BalGame g " +
            "LEFT JOIN FETCH g.applicant " +
            "WHERE (:status IS NULL OR g.status = :status) " +
            "AND (:categoryCode IS NULL OR g.categoryCode = :categoryCode) " +
            "ORDER BY g.createTime DESC, g.id DESC",
            countQuery = "SELECT COUNT(g) FROM BalGame g " +
                    "WHERE (:status IS NULL OR g.status = :status) " +
                    "AND (:categoryCode IS NULL OR g.categoryCode = :categoryCode)")
    Page<BalGame> findAdminGames(@Param("status") BalGameStatus status,
                                  @Param("categoryCode") BalCategoryCode categoryCode,
                                  Pageable pageable);

    // PUBLISHED, update_time DESC.
    List<BalGame> findByStatusOrderByUpdateTimeDescIdDesc(BalGameStatus status, Pageable pageable);

    // 예약 게시 자동 전환 대상 조회 (SCHEDULED + scheduled_at 도달)
    @Query("select g from BalGame g where g.status = :status and g.scheduledAt is not null and g.scheduledAt <= :now")
    List<BalGame> findDueScheduled(@Param("status") BalGameStatus status, @Param("now") LocalDateTime now);

    // a/b 카운트 원자적 갱신 (delta = -1, 0, +1). 총합은 응답 DTO 에서 a + b 로 계산.
    //   - 결과: 유저 투표 활동은 bal_game.update_time 을 변경시키지 않음 → 홈 정렬(update_time DESC) 에 영향 없음.
    //   - 정책: 홈 노출 순서는 관리자 액션(컨텐츠 수정·상태 변경 등 entity setter 경로) 으로만 움직임.
    @Modifying
    @Query("update BalGame g set " +
            "g.aCount = g.aCount + :aDelta, " +
            "g.bCount = g.bCount + :bDelta " +
            "where g.id = :gameId")
    int updateVoteCounts(@Param("gameId") Long gameId,
                         @Param("aDelta") int aDelta,
                         @Param("bDelta") int bDelta);

    // 댓글 카운트 원자적 갱신 (delta = +1 작성 / -1 soft delete).
    //   - 결과: 댓글 작성/삭제는 bal_game.update_time 을 변경시키지 않음 → 홈 정렬에 영향 없음.
    //   - 정책: 유저 활동(투표·댓글) 으로 홈 노출 순서가 자동으로 바뀌지 않음. 노출 순서는 관리자 액션으로만 통제.
    @Modifying
    @Query("update BalGame g set g.commentCount = g.commentCount + :delta where g.id = :gameId")
    int updateCommentCount(@Param("gameId") Long gameId, @Param("delta") int delta);
}
