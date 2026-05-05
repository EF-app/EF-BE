package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 밸런스 게임별 익명 닉네임 매핑 엔티티 (복합 PK + 게임 내 닉네임 UNIQUE)
@Getter
@Entity
@Table(name = "bal_name_map",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bnm_game_nickname", columnNames = {"game_id", "nickname"})
        })
@IdClass(BalNameMapId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalNameMap extends BaseEntity {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", foreignKey = @ForeignKey(name = "fk_map_game"))
    private BalGame game;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_map_user"))
    private User user;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Builder
    private BalNameMap(BalGame game, User user, String nickname) {
        this.game = game;
        this.user = user;
        this.nickname = nickname;
    }
}
