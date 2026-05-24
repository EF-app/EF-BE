package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.profile.entity.ProfileStatus;
import com.nokcha.efbe.domain.user.entity.BanStatus;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 유저 목록 행 DTO.
@Getter
@Builder
public class AdminUserSummaryRspDto {

    private Long id;
    private String uuid;
    private String loginId;
    private String nickname;
    private Integer age;
    private String area;            // "서울특별시 강남구" 조합 문자열 (미입력 시 null)
    private String status;          // FE UserStatus — ACTIVE / TEMP_SUSPENDED / PERMANENTLY_SUSPENDED
    private String profileStatus;
    private boolean withdraw;
    private LocalDateTime lastLoginTime;
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
