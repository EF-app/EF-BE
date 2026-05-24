package com.nokcha.efbe.domain.balGame.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 밸런스 게임 본문 엔티티
@Getter
@Entity
@Table(name = "bal_game",
        indexes = {
                @Index(name = "idx_game_category", columnList = "category_code, status"),
                @Index(name = "idx_game_status", columnList = "status"),
                @Index(name = "idx_game_status_schedule", columnList = "status, scheduled_at"),
                @Index(name = "idx_game_sched_end", columnList = "status, scheduled_end_at"),
                @Index(name = "idx_game_applicant", columnList = "applicant_id, create_time DESC")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalGame extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "option_a", nullable = false, length = 255)
    private String optionA;

    @Column(name = "option_a_desc", length = 500)
    private String optionADesc;

    // 옵션 A 표시용 이모지 (UI 장식, 비어있으면 텍스트만 표시)
    @Column(name = "option_a_emoji", length = 8)
    private String optionAEmoji;

    @Column(name = "option_b", nullable = false, length = 255)
    private String optionB;

    @Column(name = "option_b_desc", length = 500)
    private String optionBDesc;

    // 옵션 B 표시용 이모지
    @Column(name = "option_b_emoji", length = 8)
    private String optionBEmoji;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_code", nullable = false, length = 20)
    private BalCategoryCode categoryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BalGameStatus status = BalGameStatus.DRAFT;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // 게시 종료 예약 시각 — 배치가 PUBLISHED → ARCHIVED 자동 전환
    @Column(name = "scheduled_end_at")
    private LocalDateTime scheduledEndAt;

    @Column(name = "a_count", nullable = false)
    private Integer aCount = 0;

    @Column(name = "b_count", nullable = false)
    private Integer bCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    // 신청자 (탈퇴 시 NULL 처리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", foreignKey = @ForeignKey(name = "fk_game_applicant"))
    private User applicant;

    @Builder
    private BalGame(String optionA, String optionB, String optionADesc, String optionBDesc,
                    String optionAEmoji, String optionBEmoji,
                    String description, BalCategoryCode categoryCode,
                    BalGameStatus status, LocalDateTime scheduledAt, LocalDateTime scheduledEndAt,
                    User applicant) {
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionADesc = optionADesc;
        this.optionBDesc = optionBDesc;
        this.optionAEmoji = optionAEmoji;
        this.optionBEmoji = optionBEmoji;
        this.description = description;
        this.categoryCode = categoryCode;
        this.status = status == null ? BalGameStatus.DRAFT : status;
        this.scheduledAt = scheduledAt;
        this.scheduledEndAt = scheduledEndAt;
        this.applicant = applicant;
        this.aCount = 0;
        this.bCount = 0;
        this.commentCount = 0;
    }

    // 본문/카테고리/예약 일시/상태/이모지 수정
    public void updateContents(String optionA, String optionB, String optionADesc, String optionBDesc,
                               String optionAEmoji, String optionBEmoji,
                               String description, BalCategoryCode categoryCode, BalGameStatus status,
                               LocalDateTime scheduledAt, LocalDateTime scheduledEndAt) {
        if (optionA != null) this.optionA = optionA;
        if (optionB != null) this.optionB = optionB;
        if (optionADesc != null) this.optionADesc = optionADesc;
        if (optionBDesc != null) this.optionBDesc = optionBDesc;
        if (optionAEmoji != null) this.optionAEmoji = optionAEmoji;
        if (optionBEmoji != null) this.optionBEmoji = optionBEmoji;
        if (description != null) this.description = description;
        if (categoryCode != null) this.categoryCode = categoryCode;
        if (status != null) this.status = status;
        this.scheduledAt = scheduledAt;
        this.scheduledEndAt = scheduledEndAt;
    }

    public void changeStatus(BalGameStatus status) {
        this.status = status;
    }

    public void markPublished() {
        this.status = BalGameStatus.PUBLISHED;
    }

    public void markArchived() {
        this.status = BalGameStatus.ARCHIVED;
    }

    public void markHidden() {
        this.status = BalGameStatus.HIDDEN;
    }

    // 예약 취소 (SCHEDULED → DRAFT)
    public void cancelSchedule() {
        this.status = BalGameStatus.DRAFT;
        this.scheduledAt = null;
    }

    // 어드민 부분 업데이트 — 일정 변경/클리어.
    public void changeScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void changeScheduledEndAt(LocalDateTime scheduledEndAt) {
        this.scheduledEndAt = scheduledEndAt;
    }

    // 어드민 부분 업데이트 — 내용 필드. null 인 필드는 건너뜀.
    // DRAFT/SCHEDULED/HIDDEN 상태에서만.
    public void editFields(String optionA, String optionB,
                            String optionADesc, String optionBDesc,
                            String optionAEmoji, String optionBEmoji,
                            String description, BalCategoryCode categoryCode) {
        if (optionA != null) this.optionA = optionA;
        if (optionB != null) this.optionB = optionB;
        if (optionADesc != null) this.optionADesc = optionADesc;
        if (optionBDesc != null) this.optionBDesc = optionBDesc;
        if (optionAEmoji != null) this.optionAEmoji = optionAEmoji;
        if (optionBEmoji != null) this.optionBEmoji = optionBEmoji;
        if (description != null) this.description = description;
        if (categoryCode != null) this.categoryCode = categoryCode;
    }
}
