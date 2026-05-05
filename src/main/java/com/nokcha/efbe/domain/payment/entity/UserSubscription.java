package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 유저 구독 이력 엔티티 (user_subscription, 이력형 B)
// payment_logs 와 1:1 매칭. 활성 구독은 유저당 최대 1행 (Service 레이어 강제).
// 갱신/재가입/플랜 변경 시 새 row 추가 — service 가 처리.
@Getter
@Entity
@Table(name = "user_subscription",
        indexes = {
                @Index(name = "idx_sub_user_active", columnList = "user_id, is_active, end_date DESC"),
                @Index(name = "idx_sub_active_end", columnList = "is_active, end_date"),
                @Index(name = "idx_sub_bonus_due", columnList = "is_active, last_bonus_grant_at"),
                @Index(name = "idx_sub_user_time", columnList = "user_id, create_time DESC")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSubscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plan_id", nullable = false)
    private Integer planId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "last_bonus_grant_at")
    private LocalDateTime lastBonusGrantAt;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = Boolean.TRUE;

    // 프리미엄 혜택 1회라도 사용 시 TRUE (환불 가능 여부 판정)
    @Column(name = "any_feature_used", nullable = false)
    private Boolean anyFeatureUsed = Boolean.FALSE;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    // 유저 해지 요청 시각 (auto_renew OFF 시점)
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Builder
    private UserSubscription(Long userId, Integer planId, LocalDateTime startedAt, LocalDateTime endDate) {
        this.userId = userId;
        this.planId = planId;
        this.startedAt = startedAt;
        this.endDate = endDate;
        this.autoRenew = Boolean.TRUE;
        this.anyFeatureUsed = Boolean.FALSE;
        this.isActive = Boolean.TRUE;
    }

    // 자동 갱신 OFF (유저 해지 요청 시각 기록)
    public void cancel(LocalDateTime canceledAt) {
        this.autoRenew = Boolean.FALSE;
        this.canceledAt = canceledAt;
    }

    // 자동 갱신 플래그 변경 (다시 ON)
    public void enableAutoRenew() {
        this.autoRenew = Boolean.TRUE;
        this.canceledAt = null;
    }

    // 만료 처리 (배치용) — end_date 도달 시 비활성화
    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }

    // 월간 보너스 지급 시각 기록 (배치용)
    public void markBonusGranted(LocalDateTime at) {
        this.lastBonusGrantAt = at;
    }

    // 프리미엄 혜택 사용 마킹 (환불 불가 상태 전환)
    public void markFeatureUsed() {
        this.anyFeatureUsed = Boolean.TRUE;
    }

    // 환불 가능 여부 — graceDays 이내 & 혜택 미사용
    public boolean isRefundable(int graceDays) {
        if (Boolean.TRUE.equals(this.anyFeatureUsed)) return false;
        if (this.startedAt == null) return false;
        return this.startedAt.plusDays(graceDays).isAfter(LocalDateTime.now());
    }
}
