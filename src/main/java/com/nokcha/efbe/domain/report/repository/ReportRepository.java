package com.nokcha.efbe.domain.report.repository;

import com.nokcha.efbe.domain.admin.report.dto.response.AdminReportGroupKey;
import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByTargetTypeAndTargetIdAndReporter_Id(ReportTargetType targetType,
                                                         Long targetId,
                                                         Long reporterId);

    // admin 측 — status 필터 플랫 목록.
    Page<Report> findAllByStatus(ReportStatus status, Pageable pageable);

    // 같은 target 의 동일 상태 신고를 시간순으로 — 자동 대표 식별 + cascade 일괄 처리용.
    // 첫 번째 항목 = 첫 신고 = 자동 대표.
    List<Report> findAllByTargetTypeAndTargetIdAndStatusOrderByCreateTimeAsc(
            ReportTargetType targetType, Long targetId, ReportStatus status);

    // 그룹화 — (target_type, target_id) 단위 집계. JPQL projection 으로 메타만 가져옴.
    // status 필터: null 이면 전체, 값 있으면 해당 상태 신고만 집계.
    // 정렬: 첫 신고 오래된 순 (처리 우선순위).
    @Query(value = """
            SELECT new com.nokcha.efbe.domain.admin.report.dto.response.AdminReportGroupKey(
                r.targetType,
                r.targetId,
                COUNT(r),
                SUM(CASE WHEN r.status = com.nokcha.efbe.domain.report.entity.ReportStatus.PENDING THEN 1L ELSE 0L END),
                MIN(r.createTime),
                MAX(r.createTime)
            )
            FROM Report r
            WHERE (:status IS NULL OR r.status = :status)
            GROUP BY r.targetType, r.targetId
            ORDER BY MIN(r.createTime) ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT CONCAT(r.targetType, '-', r.targetId))
            FROM Report r
            WHERE (:status IS NULL OR r.status = :status)
            """)
    Page<AdminReportGroupKey> findGroupKeys(@Param("status") ReportStatus status, Pageable pageable);

    // 한 그룹의 모든 신고 — 그룹화 응답에서 각 그룹의 신고 리스트 채우기용.
    List<Report> findAllByTargetTypeAndTargetIdOrderByCreateTimeAsc(
            ReportTargetType targetType, Long targetId);
}
