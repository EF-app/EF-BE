package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 유저 상세 — 접속 이력 한 줄.
@Getter
@Builder
@Schema(description = "어드민 유저 접속 이력 한 줄")
public class AdminUserLoginLogRspDto {

    @Schema(description = "로그 PK", example = "9821")
    private Long id;

    @Schema(description = "접속 시각", example = "2026-05-23T18:42:00")
    private LocalDateTime loginAt;

    @Schema(description = "접속 IP", example = "203.0.113.42", nullable = true)
    private String ipAddress;

    @Schema(description = "접속 플랫폼 (IOS / ANDROID / WEB / UNKNOWN)", example = "IOS", nullable = true)
    private String platform;

    @Schema(description = "기기 고유 ID", example = "iPhone15,3-AB12CD34", nullable = true)
    private String deviceId;

    @Schema(description = "로그인 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "실패 사유 코드 (성공 시 null)", example = "INVALID_PASSWORD", nullable = true)
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
