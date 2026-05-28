package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.Report;
import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 평탄화 정책 — 같은 (target_type, target_id) 의 모든 PROCESSED 신고가 동일 suspension_id
@Getter
@Builder
@Schema(description = "어드민 신고 단건 상세")
public class AdminReportDetailRspDto {

    @Schema(description = "신고 PK", example = "1024")
    private Long id;

    @Schema(description = "신고 대상 유형 (포스트잇/밸런스댓글/프로필/채팅/채팅이미지)",
            example = "POST_IT")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK (target_type 에 따라 의미 다름)", example = "42")
    private Long targetId;

    @Schema(description = "신고자 유저 PK (탈퇴 시 null)", example = "17", nullable = true)
    private Long reporterId;

    @Schema(description = "다중 선택 사유 코드",
            example = "HATE,SPAM",
            nullable = true)
    private String reasonCodes;

    @Schema(description = "신고자가 자유 입력한 상세 텍스트",
            example = "욕설이 반복적으로 포함되어 있습니다.",
            nullable = true)
    private String detail;

    @Schema(description = "처리 상태 (PENDING / PROCESSED / DISMISSED)", example = "PROCESSED")
    private ReportStatus status;

    @Schema(description = "처리한 관리자 PK", example = "1", nullable = true)
    private Long adminProcessedById;

    @Schema(description = "처리한 관리자 이름", example = "관리자", nullable = true)
    private String adminProcessedByName;

    @Schema(description = "처리 시각", example = "2026-05-24T10:00:00", nullable = true)
    private LocalDateTime adminProcessedAt;

    @Schema(description = "이 신고로 이어진 user_suspension.id. 같은 그룹의 모든 PROCESSED 신고에 동일 id",
            example = "37", nullable = true)
    private Long suspensionId;

    @Schema(description = "신고 접수 시각", example = "2026-05-23T18:42:00")
    private LocalDateTime createTime;

    /* ─── enrich 필드  — 신고 상세 화면 표시용 ─── */

    @Schema(description = "신고자 닉네임. 탈퇴 등 미존재면 null", nullable = true)
    private String reporterNickname;

    @Schema(description = "신고 대상 유저 PK (POST_IT 작성자 / BAL_COMMENT 작성자 / PROFILE 본인)",
            nullable = true)
    private Long targetUserId;

    @Schema(description = "신고 대상 유저 로그인 ID", example = "test01", nullable = true)
    private String targetUserLoginId;

    @Schema(description = "신고 대상 유저 닉네임", nullable = true)
    private String targetUserNickname;

    @Schema(description = "BAL_COMMENT 일 때 부모 게임 id (admin FE 라우팅용)", nullable = true)
    private Long balGameId;

    @Schema(description = "대상 콘텐츠 미리보기 (포스트잇/댓글 본문 일부)", nullable = true)
    private String targetPreview;

    // 단순 매핑
    public static AdminReportDetailRspDto from(Report report) {
        return buildBase(report).build();
    }

    public static AdminReportDetailRspDtoBuilder buildBase(Report report) {
        return AdminReportDetailRspDto.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reporterId(report.getReporter() == null ? null : report.getReporter().getId())
                .reasonCodes(report.getReasonCodes())
                .detail(report.getDetail())
                .status(report.getStatus())
                .adminProcessedById(report.getAdminProcessedBy() == null ? null : report.getAdminProcessedBy().getId())
                .adminProcessedByName(report.getAdminProcessedBy() == null ? null : report.getAdminProcessedBy().getName())
                .adminProcessedAt(report.getAdminProcessedAt())
                .suspensionId(report.getSuspensionId())
                .createTime(report.getCreateTime());
    }
}
