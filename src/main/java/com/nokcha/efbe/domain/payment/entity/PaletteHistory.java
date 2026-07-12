package com.nokcha.efbe.domain.payment.entity;

import com.nokcha.efbe.domain.payment.model.PaletteEventType;
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
 * 팔레트 생애주기 이력 — append-only 원장
 * premium_until 변천사 복원
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "palette_history",
        indexes = {
                @Index(name = "idx_palette_user_time", columnList = "user_id, create_time"),
                @Index(name = "idx_palette_payment", columnList = "payment_id")
        })
public class PaletteHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "palette_history_id")
    private Long paletteHistoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private PaletteEventType eventType;

    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "before_until")
    private LocalDateTime beforeUntil;

    @Column(name = "after_until")
    private LocalDateTime afterUntil;

    @Column(name = "description", length = 255)
    private String description;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "create_user", updatable = false)
    private Long createUser;

    @Builder
    private PaletteHistory(Long userId, PaletteEventType eventType, Long paymentId,
                           LocalDateTime beforeUntil, LocalDateTime afterUntil, String description) {
        this.userId = userId;
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.beforeUntil = beforeUntil;
        this.afterUntil = afterUntil;
        this.description = description;
    }
}
