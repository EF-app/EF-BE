package com.nokcha.efbe.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * RevenueCat webhook 멱등 가드 — 처리한 event.id 를 저장해 재전송 중복 지급을 막는다.
 * event_id PK 라 동일 이벤트 재수신 시 INSERT 가 충돌 → 스킵.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "rc_processed_event")
public class RcProcessedEvent {

    /** RevenueCat event.id. */
    @Id
    @Column(name = "event_id", length = 120)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "app_user_id", length = 120)
    private String appUserId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Builder
    private RcProcessedEvent(String eventId, String eventType, String appUserId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.appUserId = appUserId;
    }
}
