package com.nokcha.efbe.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import java.time.LocalDateTime;

/**
 * 개인정보 파기 이력 (user_destruction_log) — 영구 보존, append-only.
 */
@Getter
@Entity
@Table(name = "user_destruction_log",
        indexes = {
                @Index(name = "idx_udl_destroyed_at", columnList = "destroyed_at"),
                @Index(name = "idx_udl_user", columnList = "user_id"),
                @Index(name = "idx_udl_withdrawal", columnList = "withdrawal_id"),
                @Index(name = "idx_udl_reason_time", columnList = "destruction_reason, destroyed_at"),
                @Index(name = "idx_udl_retry", columnList = "external_purge_status, last_retried_at")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDestructionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "withdrawal_id")
    private Long withdrawalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_uuid", nullable = false, length = 36)
    private String userUuid;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    @Column(name = "destroyed_at", nullable = false)
    private LocalDateTime destroyedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "destruction_reason", nullable = false, length = 20)
    private DestructionReason destructionReason;

    @Column(name = "destroyed_fields", columnDefinition = "JSON")
    private String destroyedFields;

    @Column(name = "retained_items", columnDefinition = "JSON")
    private String retainedItems;

    @Column(name = "photo_count_destroyed", nullable = false)
    private int photoCountDestroyed;

    @Column(name = "photo_count_failed", nullable = false)
    private int photoCountFailed;

    @Column(name = "external_purge_status", nullable = false, length = 10)
    private String externalPurgeStatus = "DONE";

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_retried_at")
    private LocalDateTime lastRetriedAt;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId = 0L;

    @Column(name = "note", length = 500)
    private String note;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Builder
    private UserDestructionLog(Long withdrawalId, Long userId, String userUuid, LocalDateTime withdrawnAt,
                              LocalDateTime destroyedAt, DestructionReason destructionReason,
                              String destroyedFields, String retainedItems,
                              int photoCountDestroyed, int photoCountFailed,
                              String externalPurgeStatus, Long operatorId, String note) {
        this.withdrawalId = withdrawalId;
        this.userId = userId;
        this.userUuid = userUuid;
        this.withdrawnAt = withdrawnAt;
        this.destroyedAt = destroyedAt;
        this.destructionReason = destructionReason;
        this.destroyedFields = destroyedFields;
        this.retainedItems = retainedItems;
        this.photoCountDestroyed = photoCountDestroyed;
        this.photoCountFailed = photoCountFailed;
        this.externalPurgeStatus = externalPurgeStatus == null ? "DONE" : externalPurgeStatus;
        this.operatorId = operatorId == null ? 0L : operatorId;
        this.note = note;
    }

    // 외부(R2 등) 파기 재시도 결과 갱신 — 재시도 배치가 호출.
    public void markRetried(String externalPurgeStatus, int photoCountDestroyed, int photoCountFailed, LocalDateTime at) {
        this.externalPurgeStatus = externalPurgeStatus;
        this.photoCountDestroyed = photoCountDestroyed;
        this.photoCountFailed = photoCountFailed;
        this.retryCount += 1;
        this.lastRetriedAt = at;
    }
}
