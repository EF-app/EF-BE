package com.nokcha.efbe.domain.admin.account.dto.response;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자 계정 응답 (목록/상세 공용)")
public class AdminAccountRspDto {

    @Schema(description = "관리자 PK", example = "1")
    private Long id;

    @Schema(description = "로그인 아이디", example = "admin01")
    private String loginId;

    @Schema(description = "관리자 실명", example = "홍길동")
    private String name;

    @Schema(description = "업무 이메일 (없으면 null)", example = "admin@ef.test", nullable = true)
    private String email;

    @Schema(description = "활성 여부 — false 면 로그인 차단", example = "true")
    private boolean isActive;

    @Schema(description = "잠금 해제 예정 시각 (NULL = 잠금 아님). 비밀번호 실패 누적으로 임시 잠금된 상태.", example = "2026-05-23T15:30:00", nullable = true)
    private LocalDateTime lockedUntil;

    @Schema(description = "최근 1시간 내 비밀번호 실패 횟수 (5회 임계 → 1시간 잠금). 잠금 정책 모니터링용.", example = "2")
    private long recentPasswordFailureCount;

    @Schema(description = "마지막 성공 로그인 시각 (admin_login_log 에서 조회, 없으면 null)", example = "2026-05-22T09:30:00", nullable = true)
    private LocalDateTime lastLoginAt;

    @Schema(description = "마지막 성공 로그인 IP (없으면 null)", example = "203.0.113.10", nullable = true)
    private String lastLoginIp;

    @Schema(description = "생성 시각", example = "2026-01-15T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "최종 수정 시각", example = "2026-05-12T07:30:00")
    private LocalDateTime updateTime;

    public static AdminAccountRspDto of(AdminAccount admin, LocalDateTime lastLoginAt, String lastLoginIp, long recentPasswordFailureCount) {
        return AdminAccountRspDto.builder()
                .id(admin.getId())
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .email(admin.getEmail())
                .isActive(admin.isActive())
                .lockedUntil(admin.getLockedUntil())
                .recentPasswordFailureCount(recentPasswordFailureCount)
                .lastLoginAt(lastLoginAt)
                .lastLoginIp(lastLoginIp)
                .createTime(admin.getCreateTime())
                .updateTime(admin.getUpdateTime())
                .build();
    }
}
