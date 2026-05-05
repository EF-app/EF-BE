package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 밸런스 게임 댓글 좋아요 엔티티 (단일 PK + 유니크 인덱스)
@Getter
@Entity
@Table(name = "bal_comment_like",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bclike_comment_user", columnNames = {"comment_id", "user_id"})
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bclike_comment"))
    private BalGameComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bclike_user"))
    private User user;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @CreatedBy
    @Column(name = "create_user", updatable = false)
    private Long createUser;

    @Builder
    private BalCommentLike(BalGameComment comment, User user) {
        this.comment = comment;
        this.user = user;
    }
}
