package com.nokcha.efbe.domain.admin.suspension.repository;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminSuspensionRepository extends JpaRepository<UserSuspension, Long> {
    // 특정 유저의 최근 N일 내 WARNING 카운트 — 수동 해제된 건 제외
    @Query("select count(s) from UserSuspension s " +
            "where s.user.id = :userId " +
            "  and s.suspensionType = com.nokcha.efbe.domain.suspension.entity.SuspensionType.WARNING " +
            "  and s.liftedByAdminId is null " +
            "  and s.createTime > :since")
    long countRecentWarnings(@Param("userId") Long userId,
                             @Param("since") LocalDateTime since);

    // 특정 유저의 마지막 TEMPORARY 제재
    @Query("select s from UserSuspension s " +
            "where s.user.id = :userId " +
            "  and s.suspensionType = com.nokcha.efbe.domain.suspension.entity.SuspensionType.TEMPORARY " +
            "  and s.liftedByAdminId is null " +
            "order by s.id desc")
    List<UserSuspension> findLatestTemporaryByUserId(@Param("userId") Long userId,
                                                     Pageable pageable);

    // (sourceTargetType, sourceTargetId) 그룹으로 부과된 제재 묶음 조회 — admin 추적용
    List<UserSuspension> findBySourceTargetTypeAndSourceTargetIdOrderByIdDesc(
            ReportTargetType sourceTargetType,
            Long sourceTargetId);

    // 관리자 전체 제재 목록 — 동적 필터
    @Query("select s from UserSuspension s " +
            "where (:userId is null or s.user.id = :userId) " +
            "  and (:userKeyword is null " +
            "       or s.user.nickname like concat('%', :userKeyword, '%') " +
            "       or s.user.uuid like concat('%', :userKeyword, '%')) " +
            "  and (:type is null or s.suspensionType = :type) " +
            "  and (:isLifted is null or s.isLifted = :isLifted) " +
            "  and (:from is null or s.createTime >= :from) " +
            "  and (:to is null or s.createTime <= :to)")
    Page<UserSuspension> searchForAdmin(@Param("userId") Long userId,
                                        @Param("userKeyword") String userKeyword,
                                        @Param("type") SuspensionType type,
                                        @Param("isLifted") Boolean isLifted,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        Pageable pageable);
}
