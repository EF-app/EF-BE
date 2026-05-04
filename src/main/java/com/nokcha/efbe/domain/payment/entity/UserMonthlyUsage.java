package com.nokcha.efbe.domain.payment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 프리미엄 월간 무료 사용 카운터 (user_monthly_usage, 복합 PK)
// (user_id, period_start, feature_code) 단위로 used_count 누적.
@Getter
@Entity
@Table(name = "user_monthly_usage",
        indexes = {@Index(name = "idx_monthly_period", columnList = "period_start")})
@IdClass(UserMonthlyUsageId.class)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMonthlyUsage {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "period_start")
    private LocalDate periodStart;

    @Id
    @Column(name = "feature_code", length = 30)
    private String featureCode;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Builder
    private UserMonthlyUsage(Long userId, LocalDate periodStart, String featureCode, Integer usedCount) {
        this.userId = userId;
        this.periodStart = periodStart;
        this.featureCode = featureCode;
        this.usedCount = usedCount == null ? 0 : usedCount;
    }

    // 카운트 + 1
    public void increase() {
        this.usedCount = (this.usedCount == null ? 0 : this.usedCount) + 1;
    }
}
