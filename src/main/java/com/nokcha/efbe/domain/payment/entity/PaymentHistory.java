package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.payment.model.PaymentStatus;
import com.nokcha.efbe.domain.payment.model.ProductType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 결제 이력 — 잉크 충전/구독 결제 공용. 결제 시점 상품 정보를 스냅샷으로 보존.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_history",
        uniqueConstraints = @UniqueConstraint(name = "uk_pg_tid", columnNames = "pg_tid"),
        indexes = {
                @Index(name = "idx_pay_user_time", columnList = "user_id, create_time"),
                @Index(name = "idx_pay_status_time", columnList = "status, create_time")
        })
public class PaymentHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // ── 상품 스냅샷 (마스터 변경과 무관하게 영수증 보존) ──
    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false, length = 10)
    private ProductType productType;

    @Column(name = "ink_amount")
    private Integer inkAmount;

    @Column(name = "duration_days")
    private Integer durationDays;

    // ── 결제 ──
    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "pg_provider", length = 30)
    private String pgProvider;

    @Column(name = "pg_tid", length = 100)
    private String pgTid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Builder
    private PaymentHistory(Long userId, Long productId, String productCode, String productName,
                           ProductType productType, Integer inkAmount, Integer durationDays,
                           int amount, String paymentMethod, String pgProvider, String pgTid) {
        this.userId = userId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.productType = productType;
        this.inkAmount = inkAmount;
        this.durationDays = durationDays;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.pgProvider = pgProvider;
        this.pgTid = pgTid;
        this.status = PaymentStatus.PENDING;
    }

    public void markPaid(String pgTid, LocalDateTime paidAt) {
        this.status = PaymentStatus.PAID;
        this.pgTid = pgTid;
        this.paidAt = paidAt;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markCanceled(LocalDateTime at) {
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = at;
    }

    public void markRefunded(LocalDateTime at) {
        this.status = PaymentStatus.REFUNDED;
        this.canceledAt = at;
    }
}
