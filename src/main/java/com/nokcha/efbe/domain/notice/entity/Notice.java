package com.nokcha.efbe.domain.notice.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeCategory category;

    @Column(nullable = false)
    private Long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeStatus status;

    @Column
    private LocalDateTime scheduledAt;

    @Column
    private LocalDateTime publishedAt;

    @Column
    private Long originalNoticeId;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Builder
    public Notice(String title, String content, NoticeCategory category, Long viewCount, NoticeStatus status,
                  LocalDateTime scheduledAt, LocalDateTime publishedAt, Long originalNoticeId, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.viewCount = viewCount;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.publishedAt = publishedAt;
        this.originalNoticeId = originalNoticeId;
        this.isPinned = isPinned;
    }

    public void update(String title, String content, NoticeCategory category, NoticeStatus status,
                       LocalDateTime scheduledAt, boolean pinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPinned = pinned;
        applyStatus(status, scheduledAt);
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void publish(LocalDateTime publishedAt) {
        this.status = NoticeStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.scheduledAt = null;
    }

    private void applyStatus(NoticeStatus status, LocalDateTime scheduledAt) {
        if (status == NoticeStatus.DRAFT) {
            this.status = NoticeStatus.DRAFT;
            this.scheduledAt = null;
            this.publishedAt = null;
            return;
        }

        if (status == NoticeStatus.SCHEDULED) {
            this.status = NoticeStatus.SCHEDULED;
            this.scheduledAt = scheduledAt;
            this.publishedAt = null;
            return;
        }

        this.status = NoticeStatus.PUBLISHED;
        this.scheduledAt = null;
        this.publishedAt = LocalDateTime.now();
    }
}
