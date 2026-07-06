package com.nokcha.efbe.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 아이템 무료 한도 카운터 — 집행용 캐시. 진실은 도메인 테이블(post_it/match_actions) + {@link ItemUsageHistory}.
 *
 * {@code period_key} 가 키에 포함돼 주기 경계마다 새 행 → 리셋 배치 불필요.
 */
@Entity
@Getter
@IdClass(ItemUsageCounterId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "item_usage_counter",
        indexes = @Index(name = "idx_counter_user_period", columnList = "user_id, period_key"))
public class ItemUsageCounter {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "item_code", length = 50)
    private String itemCode;

    @Id
    @Column(name = "period_key", length = 20)
    private String periodKey;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Builder
    private ItemUsageCounter(Long userId, String itemCode, String periodKey, int usedCount) {
        this.userId = userId;
        this.itemCode = itemCode;
        this.periodKey = periodKey;
        this.usedCount = usedCount;
    }

    public void increment() {
        this.usedCount++;
    }
}
