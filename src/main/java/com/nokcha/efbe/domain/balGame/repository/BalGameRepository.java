package com.nokcha.efbe.domain.balGame.repository;

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

// 밸런스 게임 레포지토리
// - 단순 CRUD/페이지네이션은 Spring Data JPA
// - 동적 검색·커서·조인 프로젝션은 BalGameQueryRepository (Querydsl) 로 위임
public interface BalGameRepository extends JpaRepository<BalGame, Long>, BalGameQueryRepository {

    // 단건 조회 (PESSIMISTIC_WRITE 락 - 카운트 동시성 보호용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select g from BalGame g where g.id = :id")
    Optional<BalGame> findByIdForUpdate(@Param("id") Long id);

    // 상태별 최신순 페이징
    Page<BalGame> findByStatusOrderByCreateTimeDesc(BalGameStatus status, Pageable pageable);

    // 홈 배치 — PUBLISHED, update_time DESC.
    // 정렬 의미: "관리자가 손댄(컨텐츠 수정·상태 변경) 게임이 위로". 유저 투표는 의도적으로 update_time 을 갱신하지 않음
    // (BalGameRepository.updateVoteCounts 의 [의도] 주석 참고). 즉, 노출 순서는 관리자 큐레이션으로 통제됨.
    List<BalGame> findByStatusOrderByUpdateTimeDescIdDesc(BalGameStatus status, Pageable pageable);

    // 예약 게시 자동 전환 대상 조회 (SCHEDULED + scheduled_at 도달)
    @Query("select g from BalGame g where g.status = :status and g.scheduledAt is not null and g.scheduledAt <= :now")
    List<BalGame> findDueScheduled(@Param("status") BalGameStatus status, @Param("now") LocalDateTime now);

    // a/b 카운트 원자적 갱신 (delta = -1, 0, +1). total_count 는 DB Generated Column 이므로 갱신 대상 아님.
    //
    // [의도] update_time 을 의도적으로 갱신하지 않는다.
    //   - JPQL bulk UPDATE 는 Hibernate @PreUpdate / @LastModifiedDate 콜백을 우회한다.
    //   - 결과: 유저 투표 활동은 bal_game.update_time 을 변경시키지 않음 → 홈 정렬(update_time DESC) 에 영향 없음.
    //   - 정책: 홈 노출 순서는 관리자 액션(컨텐츠 수정·상태 변경 등 entity setter 경로) 으로만 움직임.
    //   - 유저 활동(투표) 으로 게임이 자동으로 맨앞에 떠오르지 않게 하는 게 명시적 요구사항임.
    //   "고쳐야 할 버그 처럼 보일 수 있으나 정상 동작이며, set 절에 g.updateTime = CURRENT_TIMESTAMP 를 추가하지 말 것."
    @Modifying
    @Query("update BalGame g set " +
            "g.aCount = g.aCount + :aDelta, " +
            "g.bCount = g.bCount + :bDelta " +
            "where g.id = :gameId")
    int updateVoteCounts(@Param("gameId") Long gameId,
                         @Param("aDelta") int aDelta,
                         @Param("bDelta") int bDelta);

    // 댓글 카운트 원자적 갱신 (delta = +1 작성 / -1 soft delete).
    //
    // [의도] update_time 을 의도적으로 갱신하지 않는다 (updateVoteCounts 와 동일 정책).
    //   - JPQL bulk UPDATE 로 @PreUpdate / @LastModifiedDate 콜백 우회.
    //   - 결과: 댓글 작성/삭제는 bal_game.update_time 을 변경시키지 않음 → 홈 정렬에 영향 없음.
    //   - 정책: 유저 활동(투표·댓글) 으로 홈 노출 순서가 자동으로 바뀌지 않음. 노출 순서는 관리자 액션으로만 통제.
    //   "고쳐야 할 버그 처럼 보일 수 있으나 정상 동작이며, set 절에 g.updateTime 을 추가하지 말 것."
    @Modifying
    @Query("update BalGame g set g.commentCount = g.commentCount + :delta where g.id = :gameId")
    int updateCommentCount(@Param("gameId") Long gameId, @Param("delta") int delta);
}
