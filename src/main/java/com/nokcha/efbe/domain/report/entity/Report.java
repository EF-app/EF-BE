package com.nokcha.efbe.domain.report.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "report",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_report_target_reporter",
                        columnNames = {"target_type", "target_id", "reporter_id"})
        },
        indexes = {
                @Index(name = "idx_report_target", columnList = "target_type, target_id"),
                @Index(name = "idx_report_status_time", columnList = "status, create_time")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id",
            foreignKey = @ForeignKey(name = "fk_report_reporter"))
    private User reporter;

    // 다중 선택된 사유 코드. 콤마 구분 enum 모음. 예: "HATE,SPAM"
    @Column(name = "reason_codes", length = 255)
    private String reasonCodes;

    @Column(name = "detail", length = 500)
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_processed_by",
            foreignKey = @ForeignKey(name = "fk_report_admin_processor"))
    private AdminAccount adminProcessedBy;

    @Column(name = "admin_processed_at")
    private LocalDateTime adminProcessedAt;

    // 대표 신고에만 연결되는 제재 user_suspension.id. cascade 신고는 NULL.
    @Column(name = "suspension_id")
    private Long suspensionId;

    // 이 신고가 따라간 대표 신고. 대표 자신은 NULL.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_report_id",
            foreignKey = @ForeignKey(name = "fk_report_parent"))
    private Report parent;

    @Builder
    private Report(User reporter, ReportTargetType targetType, Long targetId,
                   String reasonCodes, String detail) {
        this.reporter = reporter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reasonCodes = reasonCodes;
        this.detail = detail;
        this.status = ReportStatus.PENDING;
    }

    // 관리자 처리 — PROCESSED (실제 제재로 이어진 경우 suspensionId 연결).
    public void process(AdminAccount admin, Long suspensionId) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.PROCESSED;
        this.adminProcessedBy = admin;
        this.adminProcessedAt = LocalDateTime.now();
        this.suspensionId = suspensionId;
    }

    // cascade 신고 처리 — 같은 target 의 대표 신고를 따라 자동 닫힘. suspensionId 는 채우지 않음 (대표만 연결).
    public void processAsCascade(AdminAccount admin, Report parent) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.PROCESSED;
        this.adminProcessedBy = admin;
        this.adminProcessedAt = LocalDateTime.now();
        this.parent = parent;
    }

    // 관리자 처리 — DISMISSED (제재 사유 부족 등).
    public void dismiss(AdminAccount admin) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.DISMISSED;
        this.adminProcessedBy = admin;
        this.adminProcessedAt = LocalDateTime.now();
    }
}