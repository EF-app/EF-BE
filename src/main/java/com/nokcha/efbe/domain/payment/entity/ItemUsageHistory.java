package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.domain.payment.model.UsageSource;
import com.nokcha.efbe.domain.payment.model.UserTier;
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
 * 아이템 사용 원장 — append-only. 무료+유료 전건 (통합형)
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "item_usage_history",
        indexes = {
                @Index(name = "idx_usage_user_item_time", columnList = "user_id, item_code, create_time"),
                @Index(name = "idx_usage_source", columnList = "source"),
                @Index(name = "idx_usage_ink", columnList = "ink_history_id")
        })
public class ItemUsageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_code", nullable = false, length = 50)
    private String itemCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 10)
    private UsageSource source;

    /** FREE=0, INK=차감 방울. */
    @Column(name = "ink_cost", nullable = false)
    private int inkCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_at_use", nullable = false, length = 10)
    private UserTier tierAtUse;

    /** [INK] 연결 잉크 원장 (논리 참조). */
    @Column(name = "ink_history_id")
    private Long inkHistoryId;

    /** [대인] 좋아요/답장 대상. */
    @Column(name = "target_id")
    private Long targetId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "create_user", updatable = false)
    private Long createUser;

    @Builder
    private ItemUsageHistory(Long userId, String itemCode, UsageSource source, int inkCost,
                             UserTier tierAtUse, Long inkHistoryId, Long targetId) {
        this.userId = userId;
        this.itemCode = itemCode;
        this.source = source;
        this.inkCost = inkCost;
        this.tierAtUse = tierAtUse;
        this.inkHistoryId = inkHistoryId;
        this.targetId = targetId;
    }
}
