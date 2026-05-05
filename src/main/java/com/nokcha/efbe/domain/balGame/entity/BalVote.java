package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 밸런스 게임 투표 기록 엔티티
@Getter
@Entity
@Table(name = "bal_vote",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_balvote_game_user", columnNames = {"game_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_balvote_user", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_balvote_game_choice", columnList = "game_id, choice")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalVote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, foreignKey = @ForeignKey(name = "fk_balvote_game"))
    private BalGame game;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_balvote_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "choice", nullable = false, length = 1)
    private BalVoteChoice choice;

    @Builder
    private BalVote(BalGame game, User user, BalVoteChoice choice) {
        this.game = game;
        this.user = user;
        this.choice = choice;
    }

    public void changeChoice(BalVoteChoice choice) {
        this.choice = choice;
    }
}
