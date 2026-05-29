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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_uuid", columnNames = "uuid"),
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

    @Column(nullable = false, length = 36)
    private String uuid;

    @Column(nullable = false, length = 50, unique = true)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 100)
    private String scode;

    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    @Column
    private LocalDate birth;

    @Column
    private Integer age;

    @Column(nullable = false)
    private Long areaId;

    @Column
    private LocalDateTime lastActiveAt;

    @Column
    private LocalDateTime lastNicknameChangedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;


    @Builder
    public User(String uuid,
                String loginId,
                String password,
                String phone,
                String email,
                String scode,
                String nickname,
                LocalDate birth,
                Integer age,
                Long areaId,
                LocalDateTime lastNicknameChangedAt,
                UserStatus status,
                LocalDateTime lastActiveAt) {
        this.uuid = uuid;
        this.loginId = loginId;
        this.password = password;
        this.phone = phone;
        this.email = email;
        this.scode = scode;
        this.nickname = nickname;
        this.birth = birth;
        this.age = age;
        this.areaId = areaId;
        this.lastNicknameChangedAt = lastNicknameChangedAt;
        this.status = status == null ? UserStatus.ACTIVE : status;
        this.lastActiveAt = lastActiveAt;
    }

    // 마지막 활동 시각 갱신 (주요 액션 시)
    public void updateLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    // 보안코드 설정/수정
    public void updateScode(String scode) {
        this.scode = scode;
    }

    // 비밀번호 수정 (인코딩된 값 전달)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 닉네임 변경 시각 갱신 (7일 쿨다운)
    public void markNicknameChanged(LocalDateTime when) {
        this.lastNicknameChangedAt = when;
    }

    // 상태 갱신 — SuspensionService.evaluateUserStatus() 가 호출
    public void changeStatus(UserStatus next) {
        this.status = next;
    }

    // 탈퇴 신청 (30일 유예)
    public void requestWithdrawal() {
        this.status = UserStatus.WITHDRAWING;
    }

    // 탈퇴 완료 (배치)
    public void completeWithdrawal() {
        this.status = UserStatus.WITHDRAWN;
    }

    // 편의 메서드 — UserAuthService 로그인 분기 등에서 사용
    public boolean isWithdrawnOrWithdrawing() {
        return this.status == UserStatus.WITHDRAWING || this.status == UserStatus.WITHDRAWN;
    }

    public boolean isSuspended() {
        return this.status == UserStatus.TEMPORARY || this.status == UserStatus.PERMANENT;
    }
}
