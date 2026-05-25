package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.user.entity.BanStatus;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 유저 목록 행 DTO.
@Getter
@Builder
@Schema(description = "어드민 유저 목록 행")
public class AdminUserSummaryRspDto {

    @Schema(description = "유저 PK", example = "1042")
    private Long id;

    @Schema(description = "유저 UUID (URL/링크용 식별자)", example = "0c3a8f1e-9b2f-4d77-9c0a-7b8e2f4d6a11")
    private String uuid;

    @Schema(description = "로그인 ID", example = "ef_user01")
    private String loginId;

    @Schema(description = "닉네임", example = "밤하늘공")
    private String nickname;

    @Schema(description = "나이", example = "27", nullable = true)
    private Integer age;

    @Schema(description = "지역 (\"국가 도시\" 조합, 미입력 시 null)", example = "대한민국 서울특별시", nullable = true)
    private String area;

    @Schema(description = "유저 상태 — ACTIVE / TEMP_SUSPENDED / PERMANENTLY_SUSPENDED", example = "ACTIVE")
    private String status;

    @Schema(description = "프로필 심사 상태 — APPROVED / REJECTED / PENDING (없으면 null)",
            example = "APPROVED", nullable = true)
    private String profileStatus;

    @Schema(description = "탈퇴 여부", example = "false")
    private boolean withdraw;

    @Schema(description = "마지막 로그인 시각", example = "2026-05-23T18:42:00", nullable = true)
    private LocalDateTime lastLoginTime;

    @Schema(description = "가입 시각", example = "2025-12-01T09:30:00")
    private LocalDateTime createTime;

    public static AdminUserSummaryRspDto from(User u, String area, ProfileStatus profileStatus) {
        return AdminUserSummaryRspDto.builder()
                .id(u.getId())
                .uuid(u.getUuid())
                .loginId(u.getLoginId())
                .nickname(u.getNickname())
                .age(u.getAge())
                .area(area)
                .status(toUserStatus(u.getBanStatus()))
                .profileStatus(profileStatus == null ? null : profileStatus.name())
                .withdraw(u.isWithdraw())
                .lastLoginTime(u.getLastLoginTime())
                .createTime(u.getCreateTime())
                .build();
    }

    // BE BanStatus → FE UserStatus 매핑. WARNING 은 BE 개념 없음.
    public static String toUserStatus(BanStatus banStatus) {
        if (banStatus == null) return "ACTIVE";
        return switch (banStatus) {
            case NONE -> "ACTIVE";
            case SEVEN_DAYS, THIRTY_DAYS -> "TEMP_SUSPENDED";
            case FOREVER -> "PERMANENTLY_SUSPENDED";
        };
    }
}
