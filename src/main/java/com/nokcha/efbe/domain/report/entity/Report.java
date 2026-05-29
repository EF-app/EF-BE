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
                @Index(name = "idx_report_status_time", columnList = "status, create_time"),
                // 그룹 처리 효율 — (target_type, target_id, status) 으로 같은 그룹의 PENDING 일괄 조회
                @Index(name = "idx_report_group", columnList = "target_type, target_id, status")
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


    // 이 신고로 이어진 제재 user_suspension.id. 같은 그룹의 모든 PROCESSED 신고에 동일 id 부여 (평탄화).

    @Column(name = "suspension_id")
    private Long suspensionId;

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

    // 관리자 처리 — PROCESSED. 같은 그룹의 모든 PENDING 에 동일 suspensionId 부여 (cascade 평탄화).
    public void process(AdminAccount admin, Long suspensionId) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.PROCESSED;
        this.adminProcessedBy = admin;
        this.adminProcessedAt = LocalDateTime.now();
        this.suspensionId = suspensionId;
    }

    // 관리자 처리 — DISMISSED (제재 사유 부족 등). 단건만.
    public void dismiss(AdminAccount admin) {
        if (this.status != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.REPORT_ALREADY_PROCESSED);
        }
        this.status = ReportStatus.DISMISSED;
        this.adminProcessedBy = admin;
        this.adminProcessedAt = LocalDateTime.now();
    }
}
