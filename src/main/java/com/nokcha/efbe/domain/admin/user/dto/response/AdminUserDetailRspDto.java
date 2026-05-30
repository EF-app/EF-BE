package com.nokcha.efbe.domain.admin.user.dto.response;

import com.nokcha.efbe.domain.admin.suspension.dto.response.AdminSuspensionRspDto;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.log.entity.UserLoginLog;
import com.nokcha.efbe.domain.profile.entity.UserProfileImage;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "관리자 전용 유저 단건 상세 — 기본정보 + 결제집계 + 프로필 패널 + 사진 + 접속이력")
public class AdminUserDetailRspDto {

    @Schema(description = "유저 PK", example = "1042")
    private Long id;

    @Schema(description = "유저 UUID", example = "0c3a8f1e-9b2f-4d77-9c0a-7b8e2f4d6a11")
    private String uuid;

    @Schema(description = "로그인 ID", example = "ef_user01")
    private String loginId;

    @Schema(description = "전화번호", example = "010-1234-5678", nullable = true)
    private String phone;

    @Schema(description = "이메일", example = "user@example.com", nullable = true)
    private String email;

    @Schema(description = "닉네임", example = "밤하늘공")
    private String nickname;

    @Schema(description = "나이", example = "27", nullable = true)
    private Integer age;

    @Schema(description = "생년월일", example = "1998-01-01", nullable = true)
    private LocalDate birth;

    @Schema(description = "지역 (\"시 구\" 조합)", example = "서울특별시 강남구", nullable = true)
    private String area;

    @Schema(description = "유저 상태 — ACTIVE / TEMPORARY / PERMANENT / WITHDRAWING / WITHDRAWN", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "탈퇴 여부", example = "false")
    private Boolean isWithdraw;

    @Schema(description = "탈퇴 신청 시각 (탈퇴 진행 중인 경우)", example = "2026-05-20T10:00:00", nullable = true)
    private LocalDateTime withdrawAt;

    @Schema(description = "마지막 활동 시각", example = "2026-05-23T18:42:00", nullable = true)
    private LocalDateTime lastActiveAt;

    @Schema(description = "마지막 닉네임 변경 시각", example = "2026-03-10T14:00:00", nullable = true)
    private LocalDateTime lastNicknameChangedAt;

    @Schema(description = "가입 시각", example = "2025-12-01T09:30:00")
    private LocalDateTime createTime;

    @Schema(description = "마지막 수정 시각", example = "2026-05-22T11:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "누적 결제 금액 (KRW)", example = "129000")
    private BigDecimal paymentTotal;

    @Schema(description = "프리미엄 활성 여부", example = "true")
    private boolean premium;

    @Schema(description = "프리미엄 만료 시각", example = "2026-06-15T00:00:00", nullable = true)
    private LocalDateTime premiumUntil;

    @Schema(description = "잉크 잔액 (재화)", example = "320")
    private Integer inkBalance;

    @Schema(description = "프로필 패널")
    private AdminUserProfileRspDto profile;

    @Schema(description = "프로필 사진 목록 (sortOrder 오름차순)")
    private List<AdminUserPhotoRspDto> photos;

    @Schema(description = "최근 접속 이력")
    private List<AdminUserLoginLogRspDto> recentLoginLogs;

    @Schema(description = "현재 활성 제재 1건 (가장 강한 등급). 없으면 null", nullable = true)
    private AdminSuspensionRspDto activeSuspension;

    @Schema(description = "전체 제재 이력 (최신순). 없으면 빈 배열")
    private List<AdminSuspensionRspDto> suspensions;

    @Schema(description = "최근 30일 내 WARNING 부과 건수 — admin FE 가 WARNING 부과 시 자동 에스컬레이션 예고 모달 노출용", example = "3")
    private long recentWarningCount;

    @Schema(description = "직전 TEMPORARY 제재의 일수 (해제/만료 무관, 최신 1건). 없으면 null. WARNING 부과 후 자동 에스컬레이션 등급 결정용.", example = "7", nullable = true)
    private Long lastTemporaryDurationDays;

    public static AdminUserDetailRspDto of(User u,
                                           String area,
                                           AdminUserProfileRspDto profile,
                                           List<UserProfileImage> photos,
                                           List<UserLoginLog> loginLogs,
                                           LocalDateTime withdrawAt,
                                           BigDecimal paymentTotal,
                                           boolean premium,
                                           LocalDateTime premiumUntil,
                                           Integer inkBalance,
                                           AdminSuspensionRspDto activeSuspension,
                                           List<AdminSuspensionRspDto> suspensions,
                                           long recentWarningCount,
                                           Long lastTemporaryDurationDays) {
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
                .status(u.getStatus())
                .isWithdraw(u.isWithdrawnOrWithdrawing())
                .withdrawAt(withdrawAt)
                .lastActiveAt(u.getLastActiveAt())
                .lastNicknameChangedAt(u.getLastNicknameChangedAt())
                .createTime(u.getCreateTime())
                .updateTime(u.getUpdateTime())
                .paymentTotal(paymentTotal == null ? BigDecimal.ZERO : paymentTotal)
                .premium(premium)
                .premiumUntil(premiumUntil)
                .inkBalance(inkBalance == null ? 0 : inkBalance)
                .profile(profile)
                .photos(photos.stream().map(AdminUserPhotoRspDto::from).toList())
                .recentLoginLogs(loginLogs.stream().map(AdminUserLoginLogRspDto::from).toList())
                .activeSuspension(activeSuspension)
                .suspensions(suspensions == null ? List.of() : suspensions)
                .recentWarningCount(recentWarningCount)
                .lastTemporaryDurationDays(lastTemporaryDurationDays)
                .build();
    }
}
