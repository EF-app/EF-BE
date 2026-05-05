package com.nokcha.efbe.domain.balGame.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// bal_name_map 복합 PK (game_id, user_id)
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BalNameMapId implements Serializable {
    private Long game;
    private Long user;
}