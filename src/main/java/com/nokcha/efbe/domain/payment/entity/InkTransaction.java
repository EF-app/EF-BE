package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Entity
@Table(name = "ink_transaction",
        indexes = {
                @Index(name = "idx_star_tx_user_time", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_star_tx_ref", columnList = "ref_type, ref_id")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InkTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tx_type", nullable = false, length = 15)
    private InkTxType txType;

    // + 적립 / - 차감 (부호 포함)
    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "ref_type", length = 30)
    private String refType;

    @Column(name = "ref_id")
    private Long refId;

    @Column(name = "memo")
    private String memo;

    @Builder
    private InkTransaction(Long userId, InkTxType txType, Integer amount, Integer balanceAfter,
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
