package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 밸런스 게임 신청 엔티티
@Getter
@Entity
@Table(name = "bal_apply",
        indexes = {
                @Index(name = "idx_apply_status_time", columnList = "status, create_time"),
                @Index(name = "idx_apply_user_time", columnList = "user_id, create_time DESC"),
                @Index(name = "idx_apply_category", columnList = "category_code, status")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalApply extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 신청자 (탈퇴 시 NULL 처리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_apply_user"))
    private User user;

    @Column(name = "option_a", nullable = false, length = 255)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 255)
    private String optionB;

    // 옵션 A 표시용 이모지 (선택, UI 장식)
    @Column(name = "option_a_emoji", length = 8)
    private String optionAEmoji;

    // 옵션 B 표시용 이모지
    @Column(name = "option_b_emoji", length = 8)
    private String optionBEmoji;

    // 게임 전체 배경 설명 (선택)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 20)
    private BalCategoryCode categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BalApplyStatus status = BalApplyStatus.PENDING;

    // 관리자 메모 (반려 사유 등)
    @Column(name = "admin_memo", length = 255)
    private String adminMemo;

    @Builder
    private BalApply(User user, String optionA, String optionB,
                     String optionAEmoji, String optionBEmoji,
                     String description,
                     BalCategoryCode categoryCode, BalApplyStatus status, String adminMemo) {
        this.user = user;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionAEmoji = optionAEmoji;
        this.optionBEmoji = optionBEmoji;
        this.description = description;
        this.categoryCode = categoryCode;
        this.status = status == null ? BalApplyStatus.PENDING : status;
        this.adminMemo = adminMemo;
    }

    // 신청 상태 변경 (관리자 메모 동시 갱신)
    public void decide(BalApplyStatus status, String adminMemo) {
        this.status = status;
        if (adminMemo != null) this.adminMemo = adminMemo;
    }

    // 단순 상태 변경 (메모 미변경)
    public void changeStatus(BalApplyStatus status) {
        this.status = status;
    }
}
