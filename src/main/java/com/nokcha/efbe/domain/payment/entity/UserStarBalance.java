package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 유저 별 잔액 엔티티 (user_star_balance, 1:1)
@Getter
@Entity
@Table(name = "user_star_balance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStarBalance extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "balance", nullable = false)
    private Integer balance = 0;

    @Column(name = "total_charged", nullable = false)
    private Integer totalCharged = 0;

    @Column(name = "total_used", nullable = false)
    private Integer totalUsed = 0;

    @Builder
    private UserStarBalance(Long userId) {
        this.userId = userId;
        this.balance = 0;
        this.totalCharged = 0;
        this.totalUsed = 0;
    }

    // 충전 반영 (잔액 + 누적 충전)
    public void charge(int amount) {
        if (amount <= 0) return;
        this.balance += amount;
        this.totalCharged += amount;
    }

    // 사용 반영 (잔액 - 누적 사용 +)
    public void use(int amount) {
        if (amount <= 0) return;
        if (this.balance < amount) {
            throw new IllegalStateException("insufficient star balance");
        }
        this.balance -= amount;
        this.totalUsed += amount;
    }

    // 환불 반영 (잔액 복구 - 누적 사용 감소는 하지 않음)
    public void refund(int amount) {
        if (amount <= 0) return;
        this.balance += amount;
    }
}
