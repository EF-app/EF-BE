package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.payment.model.PaymentEnvironment;
import com.nokcha.efbe.domain.payment.model.PaymentStatus;
import com.nokcha.efbe.domain.payment.model.ProductType;
import com.nokcha.efbe.domain.payment.model.StoreType;
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
 * 결제 이력 — 인앱결제(IAP) 기록. RevenueCat webhook 이 스토어 결제를 이 원장에 남김(감사·영수증).
 *
 * {@code store_transaction_id} 유니크로 멱등(중복 이벤트 방어). 상품 정보는 스냅샷으로 보존.
 * user_id/product_id 는 논리 FK.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_history",
        uniqueConstraints = @UniqueConstraint(name = "uk_store_tx", columnNames = "store_transaction_id"),
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

    @Column(name = "amount", nullable = false)
    private int amount; // 스토어가 정한 결제액(원)

    // ── 스토어(IAP) 식별 — RevenueCat webhook 기반 ──
    @Enumerated(EnumType.STRING)
    @Column(name = "store", length = 10)
    private StoreType store;

    @Column(name = "store_product_id", length = 100)
    private String storeProductId;

    @Column(name = "store_transaction_id", length = 120)
    private String storeTransactionId;

    @Column(name = "original_transaction_id", length = 120)
    private String originalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", length = 12)
    private PaymentEnvironment environment;

    @Column(name = "rc_event_type", length = 40)
    private String rcEventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Builder
    private PaymentHistory(Long userId, Long productId, String productCode, String productName,
                           ProductType productType, Integer inkAmount, Integer durationDays, int amount) {
        this.userId = userId;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.productType = productType;
        this.inkAmount = inkAmount;
        this.durationDays = durationDays;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
    }

    /** 스토어(IAP) 결제 정보로 PAID 확정 — RevenueCat webhook 지급 경로. */
    public void applyStorePaid(StoreType store, String storeProductId, String storeTransactionId,
                               String originalTransactionId, PaymentEnvironment environment,
                               String rcEventType, LocalDateTime paidAt) {
        this.store = store;
        this.storeProductId = storeProductId;
        this.storeTransactionId = storeTransactionId;
        this.originalTransactionId = originalTransactionId;
        this.environment = environment;
        this.rcEventType = rcEventType;
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markRefunded(LocalDateTime at) {
        this.status = PaymentStatus.REFUNDED;
        this.canceledAt = at;
    }
}
