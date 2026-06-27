package com.nokcha.efbe.domain.notification.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_time", columnList = "user_id, create_time DESC, id DESC"),
                @Index(name = "idx_notification_user_read", columnList = "user_id, is_read, create_time DESC"),
                @Index(name = "idx_notification_type_time", columnList = "notification_type, create_time DESC")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_notification_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 30)
    private NotificationTargetType targetType;  // 클릭 후 이동 위치 타입

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "deep_link", length = 500)
    private String deepLink;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = Boolean.FALSE;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    private Notification(User user, NotificationType type, String title, String body,
                         NotificationTargetType targetType, Long targetId, String deepLink,
                         Boolean isRead, LocalDateTime readAt, Boolean isDeleted,
                         LocalDateTime deletedAt, LocalDateTime sentAt) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.body = body;
        this.targetType = targetType;
        this.targetId = targetId;
        this.deepLink = deepLink;
        this.isRead = Boolean.TRUE.equals(isRead);
        this.readAt = readAt;
        this.isDeleted = Boolean.TRUE.equals(isDeleted);
        this.deletedAt = deletedAt;
        this.sentAt = sentAt;
    }

    public void markAsRead(LocalDateTime readAt) {
        if (Boolean.TRUE.equals(this.isRead)) return;
        this.isRead = Boolean.TRUE;
        this.readAt = readAt;
    }

    public void delete(LocalDateTime deletedAt) {
        if (Boolean.TRUE.equals(this.isDeleted)) return;
        this.isDeleted = Boolean.TRUE;
        this.deletedAt = deletedAt;
    }

    public void markSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
