package com.nokcha.efbe.domain.user.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.profile.entity.Purpose;
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

    @Column(length = 4)
    private String scode;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column
    private Integer birth;

    // 한국 나이 (휴대폰 인증 단계에서 생년월일을 받아 산출, 빠른년생 등으로 수정 가능)
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

    @Column
    private LocalDateTime lastNicknameChangeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BanStatus banStatus;

    @Builder
    public User(String uuid, String loginId, String password, String phone, String scode, String nickname, Integer birth, Integer age, Job job, boolean isWithdraw, LocalDateTime withdrawDate, Long areaId, Purpose purpose, LocalDateTime lastLoginTime, Role role, LocalDateTime lastNicknameChangeTime, BanStatus banStatus) {
        this.uuid = uuid;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.scode = scode;
        this.nickname = nickname;
        this.birth = birth;
        this.age = age;
        this.job = job;
        this.isWithdraw = isWithdraw;
        this.withdrawDate = withdrawDate;
        this.areaId = areaId;
        this.purpose = purpose;
        this.lastLoginTime = lastLoginTime;
        this.role = role;
        this.lastNicknameChangeTime = lastNicknameChangeTime;
        this.banStatus = banStatus;
    }

    // 마지막 로그인 시간 갱신
    public void updateLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    // 빠른년생 등으로 인한 한국 나이 수정 (생년월일과 별개)
    public void updateAge(Integer age) {
        this.age = age;
    }
}
