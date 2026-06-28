package com.nokcha.efbe.domain.errorLog.repository;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSource;
import com.nokcha.efbe.domain.errorLog.entity.SystemErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

// system_error_log 표준 JPA Repository (적재 save + 단건 findById + 관리자 동적 필터 조회)
public interface SystemErrorLogRepository extends JpaRepository<SystemErrorLog, Long> {

    // 관리자 목록 동적 필터 조회
    @Query("""
            select e from SystemErrorLog e
             where (:source    is null or e.errorSource = :source)
               and (:severity  is null or e.severity = :severity)
               and (:errorType is null or e.errorType like concat('%', :errorType, '%'))
               and (:userId    is null or e.userId = :userId)
               and (:adminId   is null or e.adminId = :adminId)
               and (:resolved  is null
                    or (:resolved = true  and e.resolvedAt is not null)
                    or (:resolved = false and e.resolvedAt is null))
               and (:from is null or e.occurredAt >= :from)
               and (:to   is null or e.occurredAt <  :to)
             order by e.occurredAt desc, e.id desc
            """)
    Page<SystemErrorLog> search(@Param("source") ErrorSource source,
                                @Param("severity") ErrorSeverity severity,
                                @Param("errorType") String errorType,
                                @Param("userId") Long userId,
                                @Param("adminId") Long adminId,
                                @Param("resolved") Boolean resolved,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);
}
