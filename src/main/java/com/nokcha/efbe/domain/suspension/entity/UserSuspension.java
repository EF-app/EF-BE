package com.nokcha.efbe.domain.suspension.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 유저 제재. row 1건 = 제재 1건. 부과/해제(수동·자동) 시 lift 컬럼이 UPDATE 됨.
@Getter
@Entity
@Table(name = "user_suspension",
        indexes = {
                @Index(name = "idx_susp_user_active", columnList = "user_id, is_lifted, ends_at"),
                @Index(name = "idx_susp_ends_at", columnList = "ends_at, is_lifted"),
                @Index(name = "idx_susp_admin", columnList = "create_user, create_time DESC"),
                @Index(name = "idx_susp_type_time", columnList = "suspension_type, create_time DESC"),
                @Index(name = "idx_susp_source", columnList = "source_target_type, source_target_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSuspension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_susp_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "suspension_type", nullable = false, length = 16)
    private SuspensionType suspensionType;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    // NULL = 영구정지
    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    // 제재 근거 신고 대상 타입. 임의 제재 시 NULL
    @Enumerated(EnumType.STRING)
    @Column(name = "source_target_type", length = 16)
    private ReportTargetType sourceTargetType;

    // 제재 근거 대상 PK (post_it.id / bal_game_comment.id / users.id 등). 임의 제재 시 NULL
    @Column(name = "source_target_id")
    private Long sourceTargetId;

    // 해제 시 true, 자동/수동 구분은 lifted_by_admin_id 의 null 여부로 한다.
    @Column(name = "is_lifted", nullable = false)
    private Boolean isLifted = Boolean.FALSE;

    @Column(name = "lifted_at")
    private LocalDateTime liftedAt;

    // 수동 해제한 관리자 admin_account.id. 자동만료(배치)는 null
    @Column(name = "lifted_by_admin_id")
    private Long liftedByAdminId;

    @Column(name = "lifted_reason", length = 500)
    private String liftedReason;

    @Builder
    private UserSuspension(User user,
                           SuspensionType suspensionType,
                           String reason,
                           LocalDateTime startsAt,
                           LocalDateTime endsAt,
                           ReportTargetType sourceTargetType,
                           Long sourceTargetId) {
        this.user = user;
        this.suspensionType = suspensionType;
        this.reason = reason;
        this.startsAt = startsAt == null ? LocalDateTime.now() : startsAt;
        this.endsAt = endsAt;
        this.sourceTargetType = sourceTargetType;
        this.sourceTargetId = sourceTargetId;
        this.isLifted = Boolean.FALSE;
    }

    //수동 해제 (관리자) — lifted_by_admin_id 가 채워짐
    public void liftManually(Long adminId, String liftedReason) {
        this.isLifted = Boolean.TRUE;
        this.liftedAt = LocalDateTime.now();
        this.liftedByAdminId = adminId;
        this.liftedReason = liftedReason;
    }

    // 자동 만료 (배치) — lifted_by_admin_id=null, lifted_reason=null.  화면은 lifted_by_admin_id 가 null 이면 "자동 만료" 로 표시
    public void liftAutomatically() {
        this.isLifted = Boolean.TRUE;
        this.liftedAt = LocalDateTime.now();
        this.liftedByAdminId = null;
        this.liftedReason = null;
    }

    // 활성 여부 — is_lifted=false AND (ends_at IS NULL OR ends_at > now)
    public boolean isActive() {
        if (Boolean.TRUE.equals(this.isLifted)) return false;
        return this.endsAt == null || this.endsAt.isAfter(LocalDateTime.now());
    }
}
