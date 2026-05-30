package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자 전용 유저 목록 응답 DTO")
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

    @Schema(description = "지역 (\"시 구\" 조합, 미입력 시 null)", example = "서울특별시 강남구", nullable = true)
    private String area;

    @Schema(description = "유저 상태 — ACTIVE / TEMPORARY / PERMANENT / WITHDRAWING / WITHDRAWN", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "프로필 심사 상태 — APPROVED / REJECTED / PENDING (없으면 null)",
            example = "APPROVED", nullable = true)
    private String profileStatus;

    @Schema(description = "탈퇴 여부 (WITHDRAWING 또는 WITHDRAWN)", example = "false")
    private Boolean isWithdraw;

    @Schema(description = "마지막 활동 시각", example = "2026-05-23T18:42:00", nullable = true)
    private LocalDateTime lastActiveAt;

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
                .status(u.getStatus())
                .profileStatus(profileStatus == null ? null : profileStatus.name())
                .isWithdraw(u.isWithdrawnOrWithdrawing())
                .lastActiveAt(u.getLastActiveAt())
                .createTime(u.getCreateTime())
                .build();
    }
}
