package com.nokcha.efbe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 재무 정산 일별 집계 (payment_daily_summary)
// 배치(OpsDailyRevenueScheduler 등)가 INSERT/UPDATE — 도메인 메서드는 두지 않음 (read-only 집계).
@Getter
@Entity
@Table(name = "payment_daily_summary",
        indexes = {@Index(name = "idx_pds_date_desc", columnList = "summary_date DESC")})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDailySummary {

    @Id
    @Column(name = "summary_date")
    private LocalDate summaryDate;

    @Column(name = "total_revenue", nullable = false)
    private Long totalRevenue = 0L;

    @Column(name = "star_revenue", nullable = false)
    private Long starRevenue = 0L;

    @Column(name = "sub_revenue", nullable = false)
    private Long subRevenue = 0L;

    @Column(name = "refund_amount", nullable = false)
    private Long refundAmount = 0L;

    @Column(name = "refund_count", nullable = false)
    private Integer refundCount = 0;

    @Column(name = "net_revenue", nullable = false)
    private Long netRevenue = 0L;

    @Column(name = "payment_count", nullable = false)
    private Integer paymentCount = 0;

    @Column(name = "payment_failed", nullable = false)
    private Integer paymentFailed = 0;

    @Column(name = "payment_pending_expired", nullable = false)
    private Integer paymentPendingExpired = 0;

    @Column(name = "new_subscriber", nullable = false)
    private Integer newSubscriber = 0;

    @Column(name = "renewed_subscriber", nullable = false)
    private Integer renewedSubscriber = 0;

    @Column(name = "churn_count", nullable = false)
    private Integer churnCount = 0;

    @Column(name = "active_subscriber_eod", nullable = false)
    private Integer activeSubscriberEod = 0;

    @Column(name = "dau", nullable = false)
    private Integer dau = 0;

    @Column(name = "new_users", nullable = false)
    private Integer newUsers = 0;

    @Column(name = "withdraw_users", nullable = false)
    private Integer withdrawUsers = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
