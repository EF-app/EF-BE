package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.entity.MatchAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MatchActionRepository extends JpaRepository<MatchAction, Long> {

    Optional<MatchAction> findByActorIdAndTargetId(Long actorId, Long targetId);

    /** UNIQUE(actor,target) — 변경 시 DELETE + INSERT 패턴용. */
    @Modifying
    @Query("delete from MatchAction a where a.actorId = :actorId and a.targetId = :targetId")
    void deleteByActorIdAndTargetId(@Param("actorId") Long actorId, @Param("targetId") Long targetId);

    /**
     * 오늘 (CURDATE) 본인이 한 액션 수 — 어뷰즈 가드용.
     *  ProfileChangeListener 가 "오늘 액션 ≥ N → 재계산 차단" 판단에 사용.
     *  MatchAction.createTime 은 BaseEntity 의 createTime (CreatedDate).
     */
    @Query("select count(a) from MatchAction a " +
            "where a.actorId = :actorId and a.createTime >= :startOfDay and a.createTime < :startOfNextDay")
    long countTodayByActor(@Param("actorId") Long actorId,
                           @Param("startOfDay") LocalDateTime startOfDay,
                           @Param("startOfNextDay") LocalDateTime startOfNextDay);

    /** 헬퍼 — LocalDate 받아서 자정 경계 자동 계산. */
    default long countTodayByActor(Long actorId, LocalDate today) {
        return countTodayByActor(actorId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }
}
