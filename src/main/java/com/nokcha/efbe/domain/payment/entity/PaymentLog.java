package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 결제 로그 엔티티 (payment_logs, 구독/별충전 통합)
@Getter
@Entity
@Table(name = "payment_logs",
        uniqueConstraints = {@UniqueConstraint(name = "uk_payment_order", columnNames = "order_id")},
        indexes = {
                @Index(name = "idx_pay_user_time", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_pay_status_time", columnList = "status, create_time")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 새 DDL: ON DELETE SET NULL — 유저 행 삭제 시 NULL 유지 (회계 보존)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 15)
    private PaymentType paymentType;

    @Column(name = "ref_plan_id")
    private Integer refPlanId;

    @Column(name = "star_amount")
    private Integer starAmount;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "KRW";

    @Column(name = "pg_provider", length = 30)
    private String pgProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // [v1.7] 환불 상세
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", length = 15)
    private RefundType refundType;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    @Builder
    private PaymentLog(Long userId, String orderId, PaymentType paymentType, Integer refPlanId,
                       Integer starAmount, BigDecimal amount, String currency, String pgProvider) {
        this.userId = userId;
        this.orderId = orderId;
        this.paymentType = paymentType;
        this.refPlanId = refPlanId;
        this.starAmount = starAmount;
        this.amount = amount;
        this.currency = currency == null ? "KRW" : currency;
        this.pgProvider = pgProvider;
        this.status = PaymentStatus.PENDING;
    }

    // 결제 성공 처리
    public void markSuccess() {
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = LocalDateTime.now();
    }

    // 결제 실패 처리
    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    // 환불 처리 (유형 + 사유 기록, v1.7)
    public void markRefunded(RefundType type, String reason) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAt = LocalDateTime.now();
        this.refundType = type;
        this.refundReason = reason;
    }
}
