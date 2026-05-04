package com.nokcha.efbe.domain.postIt.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;

// 포스트잇 게시글 엔티티 (post_it)
@Getter
@Entity
@Table(name = "post_it",
        uniqueConstraints = {@UniqueConstraint(name = "uk_post_uuid", columnNames = "uuid")},
        indexes = {
                @Index(name = "idx_post_active_feed", columnList = "is_hidden, is_deleted, create_time DESC"),
                @Index(name = "idx_post_pinned", columnList = "is_hidden, is_deleted, pinned_until DESC"),
                @Index(name = "idx_post_user_time", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_post_category", columnList = "category_code, is_hidden, is_deleted, create_time DESC"),
                @Index(name = "idx_post_user_cat_time", columnList = "user_id, category_code, create_time"),
                @Index(name = "idx_post_expires", columnList = "expires_at")
        })
@Check(name = "chk_lightn_not_anon", constraints = "NOT (category_code = 'LIGHTN' AND is_anonymous = TRUE)")
@Check(name = "chk_post_report_nn", constraints = "report_count >= 0")
@Check(name = "chk_post_reply_nn", constraints = "reply_count >= 0")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostIt extends BaseEntity {

    public static final String DELETED_POST_TEXT = "원문이 삭제된 포스트잇입니다";
    public static final String HIDDEN_POST_TEXT = "숨김처리된 포스트잇입니다";
    public static final int HIDE_THRESHOLD = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // [v1.6] 외부 API path 용 UUID
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_post_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 16)
    private PostCategory categoryCode;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = Boolean.FALSE;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "pinned_until")
    private LocalDateTime pinnedUntil;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = Boolean.FALSE;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    private PostIt(String uuid, User user, PostCategory categoryCode, String content,
                   Boolean isAnonymous, LocalDateTime expiresAt) {
        this.uuid = uuid;
        this.user = user;
        this.categoryCode = categoryCode;
        this.content = content;
        // 번개 카테고리는 익명 강제 불가
        this.isAnonymous = Boolean.TRUE.equals(isAnonymous) && categoryCode != PostCategory.LIGHTN;
        this.expiresAt = expiresAt;
        this.reportCount = 0;
        this.replyCount = 0;
        this.isHidden = Boolean.FALSE;
        this.isDeleted = Boolean.FALSE;
    }

    // 만료 시각 연장 (프리미엄 전환 등)
    public void extendExpires(LocalDateTime expiresAt) {
        if (expiresAt != null) this.expiresAt = expiresAt;
    }

    // 상단 고정 활성화
    public void activatePin(LocalDateTime until) {
        this.pinnedUntil = until;
    }

    // 상단 고정 해제 (배치용)
    public void clearPin() {
        this.pinnedUntil = null;
    }

    // 답장 수 ± 1
    public void increaseReplyCount() {
        this.replyCount = (this.replyCount == null ? 0 : this.replyCount) + 1;
    }

    public void decreaseReplyCount() {
        this.replyCount = Math.max(0, (this.replyCount == null ? 0 : this.replyCount) - 1);
    }

    // 신고 누적 + 임계치 도달 시 자동 숨김
    public void increaseReportAndHideIfThreshold() {
        this.reportCount = (this.reportCount == null ? 0 : this.reportCount) + 1;
        if (this.reportCount >= HIDE_THRESHOLD) this.isHidden = Boolean.TRUE;
    }

    // 작성자 Soft delete
    public void softDelete() {
        this.isDeleted = Boolean.TRUE;
    }

    // 본문 표시 정책 적용 (삭제/숨김 우선 치환)
    public String resolveDisplayContent() {
        if (Boolean.TRUE.equals(this.isHidden)) return HIDDEN_POST_TEXT;
        if (Boolean.TRUE.equals(this.isDeleted)) return DELETED_POST_TEXT;
        return this.content;
    }

    // 만료 여부
    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.isBefore(LocalDateTime.now());
    }

    // 상단 고정 활성 여부
    public boolean isPinned() {
        return this.pinnedUntil != null && this.pinnedUntil.isAfter(LocalDateTime.now());
    }

    // 번개 카테고리 여부 (DDL 의 category_code = 'LIGHTN')
    public boolean isLightning() {
        return this.categoryCode == PostCategory.LIGHTN;
    }
}
