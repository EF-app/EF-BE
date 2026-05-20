package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 유저 상세 — 접속 이력 한 줄.
@Getter
@Builder
public class AdminUserLoginLogRspDto {

    private Long id;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String platform;            // IOS / ANDROID / WEB / UNKNOWN
    private String deviceId;
    private boolean success;
    private String failureReason;

    public static AdminUserLoginLogRspDto from(UserLoginLog l) {
        return AdminUserLoginLogRspDto.builder()
                .id(l.getId())
                .loginAt(l.getLoginAt())
                .ipAddress(l.getIpAddress())
                .platform(l.getPlatform() == null ? null : l.getPlatform().name())
                .deviceId(l.getDeviceId())
                .success(l.isSuccess())
                .failureReason(l.getFailureReason() == null ? null : l.getFailureReason().name())
                .build();
    }
}
