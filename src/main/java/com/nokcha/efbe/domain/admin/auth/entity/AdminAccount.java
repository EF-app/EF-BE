package com.nokcha.efbe.domain.admin.auth.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 관리자 계정 (DDL admin_account). users 테이블과 완전 분리.
@Getter
@Entity
@Table(
        name = "admin_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_admin_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_admin_uuid", columnNames = "uuid")
        },
        indexes = {
                @Index(name = "idx_admin_role_active", columnList = "role, is_active")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String uuid;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Builder
    public AdminAccount(String uuid, String loginId, String password, String name,
                        String email, String phone, AdminRole role, boolean isActive) {
        this.uuid = uuid;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.isActive = isActive;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public boolean isLocked(LocalDateTime now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public void lock(LocalDateTime until) {
        this.lockedUntil = until;
    }

    public void unlock() {
        this.lockedUntil = null;
    }
}
