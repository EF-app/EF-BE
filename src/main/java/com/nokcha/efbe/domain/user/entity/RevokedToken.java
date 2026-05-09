package com.nokcha.efbe.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 로그아웃·강제 폐기된 JWT 의 jti 블랙리스트 (access + refresh 통합)
// 매 인증 요청에서 access jti, 매 refresh 요청에서 refresh jti 조회.
@Getter
@Entity
@Table(name = "revoked_token",
        indexes = {
                @Index(name = "idx_revoked_user", columnList = "user_id, revoked_at"),
                @Index(name = "idx_revoked_expires", columnList = "expires_at")
        })
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RevokedToken {

    @Id
    @Column(name = "jti", length = 36)
    private String jti;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_type", nullable = false, length = 10)
    private String tokenType;  // "ACCESS" or "REFRESH"

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "revoked_at", nullable = false, updatable = false)
    private LocalDateTime revokedAt;

    @Builder
    private RevokedToken(String jti, Long userId, String tokenType, LocalDateTime expiresAt) {
        this.jti = jti;
        this.userId = userId;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }
}
