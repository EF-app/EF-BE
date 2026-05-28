package com.nokcha.efbe.domain.admin.report.dto.response;

import com.nokcha.efbe.domain.report.entity.ReportStatus;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// admin 측 신고 목록 항목 — 그룹화 응답(getReportsGrouped) 의 대표 신고로 사용.
@Getter
@Builder
@Schema(description = "어드민 신고 목록 항목 — 그룹화 응답의 대표 신고")
public class AdminReportSummaryRspDto {

    @Schema(description = "신고 PK", example = "1024")
    private Long id;

    @Schema(description = "신고 대상 유형", example = "POST_IT")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK", example = "42")
    private Long targetId;

    @Schema(description = "처리 상태 (PENDING / PROCESSED / DISMISSED)", example = "PENDING")
    private ReportStatus status;

    @Schema(description = "신고자 유저 PK (탈퇴 시 null)", example = "17", nullable = true)
    private Long reporterId;

    @Schema(description = "신고 접수 시각", example = "2026-05-23T18:42:00")
    private LocalDateTime createTime;

    @Schema(description = "다중 선택 사유 코드 (쉼표 구분, 예: HATE,SPAM)",
            example = "HATE,SPAM", nullable = true)
    private String reasonCodes;

    @Schema(description = "신고자가 자유 입력한 상세 텍스트",
            example = "욕설이 반복적으로 포함되어 있습니다.", nullable = true)
    private String detail;

    // ===== enrich 필드 (그룹화 응답 전용) =====

    @Schema(description = "신고자 닉네임 (enrich)",
            example = "익명펭귄", nullable = true)
    private String reporterNickname;

    @Schema(description = "대상 콘텐츠 작성자 유저 PK (enrich)", example = "42", nullable = true)
    private Long targetUserId;

    @Schema(description = "대상 콘텐츠 작성자 로그인 ID (enrich)",
            example = "test01", nullable = true)
    private String targetUserLoginId;

    @Schema(description = "대상 콘텐츠 작성자 닉네임 (enrich)",
            example = "밤하늘공", nullable = true)
    private String targetUserNickname;

    @Schema(description = "대상이 밸런스 댓글일 때 해당 게임 PK (enrich)",
            example = "108", nullable = true)
    private Long balGameId;

    @Schema(description = "대상 콘텐츠 본문 프리뷰 (enrich, 60자 내외)",
            example = "오늘 너무 짜증나는 일이...",
            nullable = true)
    private String targetPreview;
}
