package com.nokcha.efbe.domain.blockedIdentity.entity;

import com.nokcha.efbe.domain.blockedIdentity.model.BlockReason;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 재가입 차단 원장 (blocked_identity) — 영구 보존, append-only.
 *
 * 영구정지자의 본인확인값(DI) HMAC 해시만 보관해 재가입을 차단한다. 원문 DI/CI 는 저장하지 않는다.
 * 계정이 파기(anonymize)되어도 이 원장은 남아 차단이 유지된다 → PERMANENT PII 파기와 재가입 차단을 양립.
 */
@Getter
@Entity
@Table(name = "blocked_identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_blocked_identity_di_hash", columnNames = "di_hash")
        },
        indexes = {
                @Index(name = "idx_blocked_identity_source_user", columnList = "source_user_id"),
                @Index(name = "idx_blocked_identity_reason_time", columnList = "block_reason, registered_at")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 본인확인값(DI) 의 HMAC-SHA256 해시(hex 64). 원문 DI 는 저장하지 않음.
    @Column(name = "di_hash", nullable = false, length = 64)
    private String diHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_reason", nullable = false, length = 30)
    private BlockReason blockReason;

    // 차단을 유발한 유저 (논리 FK — 유저가 파기돼도 추적 가능하도록 물리 FK 없음). 미상 시 null.
    @Column(name = "source_user_id")
    private Long sourceUserId;

    @Column(name = "note", length = 500)
    private String note;

    @CreatedDate
    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Builder
    private BlockedIdentity(String diHash, BlockReason blockReason, Long sourceUserId, String note) {
        this.diHash = diHash;
        this.blockReason = blockReason;
        this.sourceUserId = sourceUserId;
        this.note = note;
    }
}
