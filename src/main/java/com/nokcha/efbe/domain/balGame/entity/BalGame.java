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
// total_count 는 DB Generated Column (a_count + b_count) — JPA 측 insert/update 금지
@Getter
@Entity
@Table(name = "bal_game",
        uniqueConstraints = {@UniqueConstraint(name = "uk_game_uuid", columnNames = "uuid")},
        indexes = {
                @Index(name = "idx_game_category", columnList = "category_code, status"),
                @Index(name = "idx_game_status", columnList = "status"),
                @Index(name = "idx_game_status_schedule", columnList = "status, scheduled_at"),
                @Index(name = "idx_game_sched_end", columnList = "status, scheduled_end_at"),
                @Index(name = "idx_game_applicant", columnList = "applicant_id, create_time DESC"),
                @Index(name = "idx_game_total_count", columnList = "total_count DESC")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BalGame extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 외부 API path 용 UUID (순차 스캔 공격 방어)
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

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

    // Generated Column — DB 가 (a_count + b_count) 으로 자동 갱신
    @Column(name = "total_count", insertable = false, updatable = false)
    private Integer totalCount;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    // 신청자 (탈퇴 시 NULL 처리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", foreignKey = @ForeignKey(name = "fk_game_applicant"))
    private User applicant;

    @Builder
    private BalGame(String uuid, String optionA, String optionB, String optionADesc, String optionBDesc,
                    String optionAEmoji, String optionBEmoji,
                    String description, BalCategoryCode categoryCode,
                    BalGameStatus status, LocalDateTime scheduledAt, LocalDateTime scheduledEndAt,
                    User applicant) {
        this.uuid = uuid;
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

    // 댓글 카운트 갱신은 BalGameRepository.updateCommentCount(...) JPQL bulk UPDATE 를 사용한다.
    // entity setter 경로를 의도적으로 제거 — @PreUpdate 가 update_time 을 건드리는 것을 막아 홈 정렬을 관리자 큐레이션 only 로 유지.
}
