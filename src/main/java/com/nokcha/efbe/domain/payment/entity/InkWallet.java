package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 잉크 지갑 — 유저당 1행. 현재 잔액의 캐시(투영). 진실은 {@link InkHistory}
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ink_wallet")
public class InkWallet extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "total_charged", nullable = false)
    private int totalCharged;

    @Column(name = "total_used", nullable = false)
    private int totalUsed;

    @Builder
    private InkWallet(Long userId) {
        this.userId = userId;
        this.balance = 0;
        this.totalCharged = 0;
        this.totalUsed = 0;
    }

    public void charge(int amount) {
        this.balance += amount;
        this.totalCharged += amount;
    }

    public void use(int amount) {
        if (this.balance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STAR);
        }
        this.balance -= amount;
        this.totalUsed += amount;
    }

    /** 환불 복원 — 잔액만 되돌림 (누적 충전/사용 통계는 건드리지 않음). */
    public void refund(int amount) {
        this.balance += amount;
    }
}
