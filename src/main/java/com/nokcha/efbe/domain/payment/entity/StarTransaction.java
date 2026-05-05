package com.nokcha.efbe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 별 거래 내역 엔티티 (star_transaction, append-only)
@Getter
@Entity
@Table(name = "star_transaction",
        indexes = {
                @Index(name = "idx_star_tx_user_time", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_star_tx_ref", columnList = "ref_type, ref_id")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StarTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 15)
    private StarTxType txType;

    // + 적립 / - 차감 (부호 포함)
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "ref_type", length = 30)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "memo", length = 255)
    private String memo;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "create_user", updatable = false)
    private Long createUser;

    @Builder
    private StarTransaction(Long userId, StarTxType txType, Integer amount, Integer balanceAfter,
                            String refType, Long refId, String memo) {
        this.userId = userId;
        this.txType = txType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.refType = refType;
        this.refId = refId;
        this.memo = memo;
    }
}
