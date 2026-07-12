package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.domain.payment.model.InkTxType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 잉크 원장 — append-only 진실. 잔액이 왜 이렇게 됐는지의 근거.
 *
 * 수정 없음 → create_time/create_user 만 audit. {@code amount} 부호 델타(+충전/-사용),
 * {@code balance_after} 반영 후 잔액. item_code/payment_id 는 논리 참조.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ink_history",
        indexes = {
                @Index(name = "idx_ink_user_time", columnList = "user_id, create_time"),
                @Index(name = "idx_ink_payment", columnList = "payment_id")
        })
public class InkHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ink_history_id")
    private Long inkHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 15)
    private InkTxType txType;

    /** 부호 델타 (+충전/-사용). */
    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    /** [USE] 사용 아이템 (논리 참조). */
    @Column(name = "item_code", length = 50)
    private String itemCode;

    /** [CHARGE] 출처 결제 (논리 참조). */
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "description", length = 255)
    private String description;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "create_user", updatable = false)
    private Long createUser;

    @Builder
    private InkHistory(Long userId, InkTxType txType, int amount, int balanceAfter,
                       String itemCode, Long paymentId, String description) {
        this.userId = userId;
        this.txType = txType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.itemCode = itemCode;
        this.paymentId = paymentId;
        this.description = description;
    }
}
