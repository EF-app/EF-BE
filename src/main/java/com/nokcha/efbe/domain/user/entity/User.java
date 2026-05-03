package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_login_id", columnNames = "login_id"),
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phone"),
                @UniqueConstraint(name = "uk_users_nickname", columnNames = "nickname")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String uuid;

    @Column(nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column
    private String email;

    @Column(length = 4)
    private String scode;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column
    private Integer birth;

    @Column(nullable = false)
    private boolean isWithdraw;

    @Column
    private LocalDateTime withdrawDate;

    @Column(nullable = false)
    private Long areaId;

    @Column
    private LocalDateTime lastLoginTime;

    @Column
    private LocalDateTime lastNicknameChangeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BanStatus banStatus;

    @Builder
    public User(String uuid, String loginId, String password, String phone, String email, String scode, String nickname, Integer birth, boolean isWithdraw, LocalDateTime withdrawDate, Long areaId, LocalDateTime lastLoginTime, LocalDateTime lastNicknameChangeTime, BanStatus banStatus) {
        this.uuid = uuid;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.scode = scode;
        this.nickname = nickname;
        this.birth = birth;
        this.isWithdraw = isWithdraw;
        this.withdrawDate = withdrawDate;
        this.areaId = areaId;
        this.lastLoginTime = lastLoginTime;
        this.lastNicknameChangeTime = lastNicknameChangeTime;
        this.banStatus = banStatus;
    }

    // 마지막 로그인 시간 갱신
    public void updateLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
