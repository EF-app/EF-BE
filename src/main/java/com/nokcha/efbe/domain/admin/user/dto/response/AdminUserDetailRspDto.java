package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// 어드민 유저 단건 상세 DTO
@Getter
@Builder
public class AdminUserDetailRspDto {

    private Long id;
    private String uuid;
    private String loginId;
    private String phone;
    private String email;
    private String nickname;
    private Integer age;
    private Integer birth;
    private String area;            // "서울특별시 강남구" 조합 문자열 (미입력 시 null)
    private String status;
    private boolean withdraw;
    private LocalDateTime withdrawAt;
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastNicknameChangeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 결제 · 구독 집계
    private BigDecimal paymentTotal;
    private boolean premium;
    private LocalDateTime premiumUntil;
    private Integer inkBalance;

    private AdminUserProfileRspDto profile;
    private List<AdminUserPhotoRspDto> photos;
    private List<AdminUserLoginLogRspDto> recentLoginLogs;

    public static AdminUserDetailRspDto of(User u,
                                           String area,
                                           AdminUserProfileRspDto profile,
                                           List<UserProfileImage> photos,
                                           List<UserLoginLog> loginLogs,
                                           LocalDateTime withdrawAt,
                                           BigDecimal paymentTotal,
                                           boolean premium,
                                           LocalDateTime premiumUntil,
                                           Integer inkBalance) {
        return AdminUserDetailRspDto.builder()
                .id(u.getId())
                .uuid(u.getUuid())
                .loginId(u.getLoginId())
                .phone(u.getPhone())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .age(u.getAge())
                .birth(u.getBirth())
                .area(area)
                .status(AdminUserSummaryRspDto.toUserStatus(u.getBanStatus()))
                .withdraw(u.isWithdraw())
                .withdrawAt(withdrawAt)
                .lastLoginTime(u.getLastLoginTime())
                .lastNicknameChangeTime(u.getLastNicknameChangeTime())
                .createTime(u.getCreateTime())
                .updateTime(u.getUpdateTime())
                .paymentTotal(paymentTotal == null ? BigDecimal.ZERO : paymentTotal)
                .premium(premium)
                .premiumUntil(premiumUntil)
                .inkBalance(inkBalance == null ? 0 : inkBalance)
                .profile(profile)
                .photos(photos.stream().map(AdminUserPhotoRspDto::from).toList())
                .recentLoginLogs(loginLogs.stream().map(AdminUserLoginLogRspDto::from).toList())
                .build();
    }
}
