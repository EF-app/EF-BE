package com.nokcha.efbe.domain.balGame.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// 익명 닉네임 단어 사전 엔티티 (code_nickname_word 테이블, v2.0 사양)
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
public class CodeNicknameWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "word", nullable = false, length = 30)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false,
            columnDefinition = "ENUM('ADJ','ANIMAL','FOOD','NATURE') NOT NULL")
    private CodeNicknameWordType type;

    @Column(name = "is_active", nullable = false,
            columnDefinition = "BOOLEAN NOT NULL DEFAULT TRUE")
    private Boolean isActive = Boolean.TRUE;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time", nullable = false,
            columnDefinition = "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateTime;

    @Builder
    private CodeNicknameWord(String word, CodeNicknameWordType type, Boolean isActive) {
        this.word = word;
        this.type = type;
        this.isActive = isActive == null ? Boolean.TRUE : isActive;
    }

    // 풀 제외 / 재포함 토글 (관리자용)
    public void changeActive(boolean active) {
        this.isActive = active;
    }
}
