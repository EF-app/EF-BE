package com.efbe.domain.user.entity;

import com.efbe.common.entity.BaseEntity;
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

    @Column(nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column(length = 4)
    private String scode;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Job job;

    @Column(nullable = false)
    private boolean isWithdraw;

    @Column
    private LocalDateTime withdrawDate;

    @Column(nullable = false)
    private Long areaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Purpose purpose;

    @Column
    private LocalDateTime lastLoginTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;


    @Builder
    public User(String loginId, String password, String phone, String scode, String nickname, Integer age, Job job, boolean isWithdraw, LocalDateTime withdrawDate, Long areaId, Purpose purpose, LocalDateTime lastLoginTime, Role role) {
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.scode = scode;
        this.nickname = nickname;
        this.age = age;
        this.job = job;
        this.isWithdraw = isWithdraw;
        this.withdrawDate = withdrawDate;
        this.areaId = areaId;
        this.purpose = purpose;
        this.lastLoginTime = lastLoginTime;
        this.role = role;
    }

    // 마지막 로그인 시간 갱신
    public void updateLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
