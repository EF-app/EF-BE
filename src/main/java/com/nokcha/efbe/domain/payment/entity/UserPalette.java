package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 팔레트 구독 상태 — 유저당 1행. 캐시/상태
 *
 * 프리미엄 = {@code premium_until > now} · 자동갱신 = {@code auto_renew} ·
 * 해지했지만 유지중 = {@code auto_renew=false AND premium_until > now}.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_palette",
        indexes = {
                @Index(name = "idx_palette_renew_due", columnList = "auto_renew, premium_until"),
                @Index(name = "idx_palette_expiry", columnList = "premium_until")
        })
public class UserPalette extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    /** null 이거나 과거면 무료. */
    @Column(name = "premium_until")
    private LocalDateTime premiumUntil;

    @Column(name = "auto_renew", nullable = false)
    private boolean autoRenew;

    /** 해지 요청 시각 (표시/감사). */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Builder
    private UserPalette(Long userId) {
        this.userId = userId;
        this.autoRenew = false;
    }

    public boolean isPremium(LocalDateTime now) {
        return premiumUntil != null && premiumUntil.isAfter(now);
    }

    /** 신규/연장 결제 성공 — 만료 연장 + 자동갱신 on + 해지플래그 클리어. */
    public void applyPurchase(LocalDateTime newUntil) {
        this.premiumUntil = newUntil;
        this.autoRenew = true;
        this.canceledAt = null;
    }

    /** 자동갱신 해지 — premium_until 은 유지, auto_renew 만 off. */
    public void cancel(LocalDateTime at) {
        this.autoRenew = false;
        this.canceledAt = at;
    }

    /** 해지 철회. */
    public void reactivate() {
        this.autoRenew = true;
        this.canceledAt = null;
    }
}
