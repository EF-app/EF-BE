package com.nokcha.efbe.domain.admin.auth.entity;

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


// 관리자 로그인 시도 기록 (DDL admin_login_log). 성공/실패 모두 남김.
@Getter
@Entity
@Table(
        name = "admin_login_log",
        indexes = {
                @Index(name = "idx_login_log_admin_time", columnList = "admin_id, login_at"),
                @Index(name = "idx_login_log_ip_time", columnList = "ip_address, login_at"),
                @Index(name = "idx_login_log_failure", columnList = "failure_reason, login_at, is_success")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관리자 FK (존재하지 않는 login_id 시도 시 NULL)
    @Column(name = "admin_id")
    private Long adminId;

    // 로그인 시도한 ID (실패 케이스 추적용)
    @Column(name = "login_id_attempt", nullable = false, length = 50)
    private String loginIdAttempt;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "is_success", nullable = false)
    private boolean isSuccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 30)
    private AdminLoginFailureReason failureReason;

    @Column(name = "failure_detail", length = 255)
    private String failureDetail;

    // TOTP 2FA 통과 여부. Phase 2
    @Column(name = "totp_verified")
    private Boolean totpVerified;

    @Builder
    public AdminLoginLog(Long adminId, String loginIdAttempt, LocalDateTime loginAt,
                         String ipAddress, String userAgent,
                         boolean isSuccess, AdminLoginFailureReason failureReason,
                         String failureDetail, Boolean totpVerified) {
        this.adminId = adminId;
        this.loginIdAttempt = loginIdAttempt;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isSuccess = isSuccess;
        this.failureReason = failureReason;
        this.failureDetail = failureDetail;
        this.totpVerified = totpVerified;
    }
}
