package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "code_nickname_word",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nickword_word", columnNames = "word")
        },
        indexes = {
                @Index(name = "idx_nickword_type_active", columnList = "type, is_active")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeNicknameWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "word", nullable = false, length = 30)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CodeNicknameWordType type;

    @Column(name = "is_active", nullable = false,
            columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean isActive = Boolean.TRUE;

    @Builder
    private CodeNicknameWord(String word, CodeNicknameWordType type, Boolean isActive) {
        this.word = word;
        this.type = type;
        this.isActive = isActive == null ? Boolean.TRUE : isActive;
    }
}
