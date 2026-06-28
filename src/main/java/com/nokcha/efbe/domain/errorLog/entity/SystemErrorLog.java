package com.nokcha.efbe.domain.errorLog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 공통 에러 적재 테이블.
@Getter
@Entity
@Table(
        name = "system_error_log",
        indexes = {
                @Index(name = "idx_err_source_time", columnList = "error_source, occurred_at"),
                @Index(name = "idx_err_type_time", columnList = "error_type, occurred_at"),
                @Index(name = "idx_err_user_time", columnList = "user_id, occurred_at"),
                @Index(name = "idx_err_admin_time", columnList = "admin_id, occurred_at"),
                @Index(name = "idx_err_severity_time", columnList = "severity, occurred_at"),
                @Index(name = "idx_err_unresolved_time", columnList = "resolved_at, occurred_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_source", nullable = false, length = 30)
    private ErrorSource errorSource;

    @Column(name = "error_type", nullable = false, length = 150)
    private String errorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10)
    private ErrorSeverity severity;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "request_url", length = 500)
    private String requestUrl;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "error_class", nullable = false, length = 200)
    private String errorClass;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stacktrace", columnDefinition = "TEXT")
    private String stacktrace;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Builder
    public SystemErrorLog(ErrorSource errorSource, String errorType, ErrorSeverity severity,
                          Integer httpStatus, String requestUrl, Long userId, Long adminId,
                          String errorClass, String errorMessage, String stacktrace, String metadata,
                          LocalDateTime occurredAt) {
        this.errorSource = errorSource;
        this.errorType = errorType;
        this.severity = severity;
        this.httpStatus = httpStatus;
        this.requestUrl = requestUrl;
        this.userId = userId;
        this.adminId = adminId;
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
        this.stacktrace = stacktrace;
        this.metadata = metadata;
        this.occurredAt = occurredAt;
    }

    /** 관리자 화면에서 미해결 에러를 해결 처리할 때 호출. */
    public void resolve(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
