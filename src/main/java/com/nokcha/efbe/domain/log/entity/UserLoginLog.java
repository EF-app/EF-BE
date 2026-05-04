package com.nokcha.efbe.domain.log.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "user_login_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(nullable = false, length = 50)
    private String loginIdAttempt;

    @Column(nullable = false)
    private LocalDateTime loginAt;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 100)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LoginPlatform platform;

    @Column(nullable = false)
    private boolean isSuccess;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private LoginFailureReason failureReason;

    @Column(nullable = false)
    private boolean isScodeStep;

    @Builder
    public UserLoginLog(Long userId, String loginIdAttempt, LocalDateTime loginAt, String ipAddress, String deviceId, LoginPlatform platform, boolean isSuccess, LoginFailureReason failureReason, boolean isScodeStep) {
        this.userId = userId;
        this.loginIdAttempt = loginIdAttempt;
        this.loginAt = loginAt;
        this.ipAddress = ipAddress;
        this.deviceId = deviceId;
        this.platform = platform;
        this.isSuccess = isSuccess;
        this.failureReason = failureReason;
        this.isScodeStep = isScodeStep;
    }
}
