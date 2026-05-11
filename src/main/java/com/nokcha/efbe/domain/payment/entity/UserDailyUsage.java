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

// 일일 사용량 카운터 (user_daily_usage, 복합 PK)
// (user_id, usage_date, action_code) 단위로 used_count 누적.
// 자정 0시에 새 row 가 자동 생성되어 한도가 리셋되는 효과.
@Getter
@Entity
@Table(name = "user_daily_usage",
        indexes = {@Index(name = "idx_daily_usage_date", columnList = "usage_date")})
@IdClass(UserDailyUsageId.class)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDailyUsage {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "usage_date")
    private LocalDate usageDate;

    @Id
    @Column(name = "action_code", length = 40)
    private String actionCode;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Builder
    private UserDailyUsage(Long userId, LocalDate usageDate, String actionCode, Integer usedCount) {
        this.userId = userId;
        this.usageDate = usageDate;
        this.actionCode = actionCode;
        this.usedCount = usedCount == null ? 0 : usedCount;
    }

    // 카운트 + 1
    public void increase() {
        this.usedCount = (this.usedCount == null ? 0 : this.usedCount) + 1;
    }
}
