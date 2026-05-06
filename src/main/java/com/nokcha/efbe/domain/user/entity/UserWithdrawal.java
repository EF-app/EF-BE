package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user_withdrawal",
        indexes = {
                @Index(name = "idx_uw_user_status", columnList = "user_id,status,requested_at"),
                @Index(name = "idx_uw_scheduled", columnList = "status,scheduled_destroy_at"),
                @Index(name = "idx_uw_reason_time", columnList = "reason_category,requested_at"),
                @Index(name = "idx_uw_admin", columnList = "forced_by_admin_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWithdrawal extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WithdrawStatus status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "scheduled_destroy_at")
    private LocalDateTime scheduledDestroyAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category")
    private WithdrawReason withdrawReason;

    @Column(name = "detail_text", columnDefinition = "LONGTEXT")
    private String detailText;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    @Column(name = "forced_by_admin_id")
    private Long forceByAdminId;

    @Column(name = "force_reason")
    private String forceReason;

    @Builder
    public UserWithdrawal(Long userId, WithdrawStatus status, LocalDateTime requestedAt, LocalDateTime cancelledAt,
                          LocalDateTime completedAt, LocalDateTime scheduledDestroyAt, WithdrawReason withdrawReason,
                          String detailText, String requestIp, Long forceByAdminId, String forceReason) {
        this.userId = userId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.cancelledAt = cancelledAt;
        this.completedAt = completedAt;
        this.scheduledDestroyAt = scheduledDestroyAt;
        this.withdrawReason = withdrawReason;
        this.detailText = detailText;
        this.requestIp = requestIp;
        this.forceByAdminId = forceByAdminId;
        this.forceReason = forceReason;
    }

    public void request(WithdrawReason withdrawReason, String detailText, String requestIp, LocalDateTime requestedAt) {
        this.status = WithdrawStatus.REQUESTED;
        this.withdrawReason = withdrawReason;
        this.detailText = detailText;
        this.requestIp = requestIp;
        this.requestedAt = requestedAt;
        this.cancelledAt = null;
        this.completedAt = null;
        this.scheduledDestroyAt = requestedAt.plusDays(30);
        this.forceByAdminId = null;
        this.forceReason = null;
    }

    public void cancel(LocalDateTime cancelledAt, Long adminId, String reason) {
        this.status = WithdrawStatus.CANCELED;
        this.cancelledAt = cancelledAt;
        this.completedAt = null;
        this.scheduledDestroyAt = null;
        this.forceByAdminId = adminId;
        this.forceReason = reason;
    }

    public void complete(Long adminId, String reason, LocalDateTime completedAt) {
        this.status = WithdrawStatus.COMPLETED;
        this.completedAt = completedAt;
        this.cancelledAt = null;
        this.forceByAdminId = adminId;
        this.forceReason = reason;
    }
}
