package com.nokcha.efbe.domain.admin.auth.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "admin_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_login_id", columnNames = "login_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Builder
    public AdminAccount(String loginId, String password, String name, String email, boolean isActive) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.isActive = isActive;
    }

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void lock(LocalDateTime until) {
        this.lockedUntil = until;
    }

    // 잠금 해제 — 관리자 강제 해제용
    public void unlock() {
        this.lockedUntil = null;
    }

    // 관리자 계정 관리
    public void updateBasicInfo(String email) {
        if (email != null) this.email = email;
    }

    // 활성/비활성 토글
    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // 비밀번호 변경
    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
