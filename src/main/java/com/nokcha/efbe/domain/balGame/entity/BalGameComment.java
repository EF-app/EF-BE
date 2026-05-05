package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 밸런스 게임 댓글 엔티티
@Getter
@Entity
@Table(name = "bal_comment",
        uniqueConstraints = {@UniqueConstraint(name = "uk_bgc_uuid", columnNames = "uuid")},
        indexes = {
                @Index(name = "idx_bc_game_create_time", columnList = "game_id, create_time"),
                @Index(name = "idx_bc_game_parent", columnList = "game_id, parent_id"),
                @Index(name = "idx_bc_hidden_filter", columnList = "game_id, is_hidden, is_deleted")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalGameComment extends BaseEntity {

    public static final String DELETED_CONTENT_TEXT = "삭제된 댓글입니다";
    public static final String HIDDEN_CONTENT_TEXT = "숨김처리된 댓글입니다";
    public static final int HIDE_THRESHOLD = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 외부 API path 용 UUID (신고/삭제)
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_game"))
    private BalGame game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_comment_user"))
    private User user;

    // 부모 댓글 (대댓글이 아닌 경우 NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "fk_comment_parent"))
    private BalGameComment parent;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    // 신고 10회 누적 시 트리거가 TRUE 설정
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = Boolean.FALSE;

    // 신고 누적 (트리거 자동 증가)
    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Builder
    private BalGameComment(String uuid, BalGame game, User user, BalGameComment parent,
                           String nickname, String content) {
        this.uuid = uuid;
        this.game = game;
        this.user = user;
        this.parent = parent;
        this.nickname = nickname;
        this.content = content;
        this.isDeleted = Boolean.FALSE;
        this.likesCount = 0;
        this.reportCount = 0;
        this.isHidden = Boolean.FALSE;
    }

    public void softDelete() {
        this.isDeleted = Boolean.TRUE;
    }

    public void increaseLikes() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    public void decreaseLikes() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }

    public void increaseReportAndHideIfThreshold() {
        this.reportCount = (this.reportCount == null ? 0 : this.reportCount) + 1;
        if (this.reportCount >= HIDE_THRESHOLD) {
            this.isHidden = Boolean.TRUE;
        }
    }

    public String resolveDisplayContent() {
        if (Boolean.TRUE.equals(this.isHidden)) return HIDDEN_CONTENT_TEXT;
        if (Boolean.TRUE.equals(this.isDeleted)) return DELETED_CONTENT_TEXT;
        return this.content;
    }
}
