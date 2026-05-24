package com.nokcha.efbe.domain.block.entity;

import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 유저 간 차단 관계 엔티티 (block)
// blocker(차단한 유저) 가 blocked(차단당한 유저) 를 차단. (blocker, blocked) 쌍은 유일.
@Getter
@Entity
@Table(
        name = "block",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ub_pair", columnNames = {"blocker_id", "blocked_id"})
        },
        indexes = {
                @Index(name = "idx_ub_blocker", columnList = "blocker_id, create_time DESC"),
                @Index(name = "idx_ub_blocked", columnList = "blocked_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 차단한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ub_blocker"))
    private User blocker;

    // 차단당한 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ub_blocked"))
    private User blocked;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @Builder
    private Block(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }
}
