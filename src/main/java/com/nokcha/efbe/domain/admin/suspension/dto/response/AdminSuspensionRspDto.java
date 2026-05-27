package com.nokcha.efbe.domain.admin.suspension.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "관리자 제재 응답")
public class AdminSuspensionRspDto {

    @Schema(description = "제재 row PK", example = "42")
    private Long id;

    @Schema(description = "제재 대상 유저 PK", example = "1042")
    private Long userId;

    @Schema(description = "대상 유저 UUID — admin 상세 페이지 라우팅용", example = "0c3a8f1e-...", nullable = true)
    private String userUuid;

    @Schema(description = "대상 유저 로그인 ID", example = "ef_user01", nullable = true)
    private String userLoginId;

    @Schema(description = "대상 유저 닉네임", example = "밤하늘공", nullable = true)
    private String userNickname;

    @Schema(description = "제재 유형", example = "TEMPORARY")
    private SuspensionType suspensionType;

    @Schema(description = "제재 사유", example = "허위 정보 게시")
    private String reason;

    @Schema(description = "제재 시작 시각", example = "2026-05-26T10:00:00")
    private LocalDateTime startsAt;

    @Schema(description = "제재 종료 시각. WARNING=시작+30일, TEMPORARY=시작+durationDays, PERMANENT=null",
            example = "2026-06-02T10:00:00", nullable = true)
    private LocalDateTime endsAt;

    @Schema(description = "제재 근거 신고 대상 타입. 임의 제재 시 null", example = "POST_IT", nullable = true)
    private ReportTargetType sourceTargetType;

    @Schema(description = "제재 근거 대상 PK", example = "1234", nullable = true)
    private Long sourceTargetId;

    @Schema(description = "해제 여부 (수동/자동 무관)", example = "false")
    private Boolean isLifted;

    @Schema(description = "해제 시각", example = "2026-05-30T15:00:00", nullable = true)
    private LocalDateTime liftedAt;

    @Schema(description = "수동 해제한 관리자 admin_account.id. 자동만료/미해제면 null", example = "3", nullable = true)
    private Long liftedByAdminId;

    @Schema(description = "수동 해제한 관리자 이름. admin_account.name. 자동만료/미해제면 null", example = "김관리", nullable = true)
    private String liftedByAdminName;

    @Schema(description = "수동 해제 사유. 자동만료면 null", example = "이의 신청 수용", nullable = true)
    private String liftedReason;

    @Schema(description = "현재 활성 여부 (is_lifted=false AND (ends_at IS NULL OR ends_at > now))", example = "true")
    private boolean active;

    @Schema(description = "부과 관리자 admin_account.id. 자동 에스컬레이션이면 0(시스템)", example = "3")
    private Long createUser;

    @Schema(description = "부과 관리자 이름. admin_account.name. 자동 에스컬레이션(create_user=0)이면 '시스템'", example = "김관리", nullable = true)
    private String createdByAdminName;

    @Schema(description = "부과 시각", example = "2026-05-26T10:00:00")
    private LocalDateTime createTime;


    //단건 변환 — admin 이름은 AdminSuspensionService 가 채워 전달.
    public static AdminSuspensionRspDto from(UserSuspension s,
                                             String createdByAdminName,
                                             String liftedByAdminName) {
        return AdminSuspensionRspDto.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .userUuid(s.getUser().getUuid())
                .userLoginId(s.getUser().getLoginId())
                .userNickname(s.getUser().getNickname())
                .suspensionType(s.getSuspensionType())
                .reason(s.getReason())
                .startsAt(s.getStartsAt())
                .endsAt(s.getEndsAt())
                .sourceTargetType(s.getSourceTargetType())
                .sourceTargetId(s.getSourceTargetId())
                .isLifted(Boolean.TRUE.equals(s.getIsLifted()))
                .liftedAt(s.getLiftedAt())
                .liftedByAdminId(s.getLiftedByAdminId())
                .liftedByAdminName(liftedByAdminName)
                .liftedReason(s.getLiftedReason())
                .active(s.isActive())
                .createUser(s.getCreateUser())
                .createdByAdminName(createdByAdminName)
                .createTime(s.getCreateTime())
                .build();
    }
}
