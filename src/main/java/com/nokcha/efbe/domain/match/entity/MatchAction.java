package com.nokcha.efbe.domain.match.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.match.model.MatchActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * match_actions 엔티티 — 단방향 액션 (LIKE / PASS / SUPER_LIKE / POWER_MESSAGE).
 *  한 페어(actor → target) 당 활성 액션 1개 정책
 *
 *  tagsJson (LIKE 류만 채움):
 *    - 액션 시점 actor 관점 매칭 태그 freeze (MatchCalculator + TagDisplayFormatter)
 *    - 카드 표시 — "내가 누른 좋아요" 는 그대로, "받은 좋아요" 는 표시 시점 #내가/#나를 반전
 *    - PASS 는 null (카드 노출 X)
 *
 *  ※ mutual 성사 정보는 match_results 테이블에 별도 보존  match_actions 의 양방향 row 로
 *    mutual 자체는 판별 가능하지만 cleanup 30일 정책 이후엔 match_results 가 영구 보존.
 */
@Getter
@Entity
@Table(
        name = "match_actions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_action_actor_targ et",
                columnNames = {"actor_id", "target_id"}
        ),
        indexes = {
                @Index(name = "idx_action_actor_type",
                        columnList = "actor_id, action_type, create_time DESC"),
                @Index(name = "idx_action_target_lookup",
                        columnList = "target_id, action_type, create_time DESC"),
                @Index(name = "idx_action_expires", columnList = "expires_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_id", nullable = false)
    private Long actorId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private MatchActionType actionType;

    /** PASS 만 NOT NULL (쿨다운 만료 시각). 그 외 NULL. */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** LIKE/SUPER_LIKE/POWER_MESSAGE — actor 관점 매칭 태그 JSON freeze. PASS 는 NULL. */
    @Column(name = "tags_json", columnDefinition = "JSON")
    private String tagsJson;

    @Builder
    private MatchAction(Long actorId, Long targetId, MatchActionType actionType,
                        LocalDateTime expiresAt, String tagsJson) {
        this.actorId = actorId;
        this.targetId = targetId;
        this.actionType = actionType;
        this.expiresAt = expiresAt;
        this.tagsJson = tagsJson;
    }

    /**
     * 내가 누른 좋아요 취소 / mutual 카드 cancel — LIKE/SUPER_LIKE 행을 PASS 로 UPDATE.
     *  - action_type = PASS, expires_at = NOW + cooldown, tags_json = NULL (정책 일관)
     *  - update_time 은 BaseEntity @LastModifiedDate 자동
     *  - row id / create_time 보존 — 매칭 풀 제외 시점은 그대로
     *
     *  SUPER_LIKE 도 동일 처리 — 별 환불 X
     */
    public void changeToPass(LocalDateTime expiresAt) {
        this.actionType = MatchActionType.PASS;
        this.expiresAt  = expiresAt;
        this.tagsJson   = null;
    }

    /**
     * mutual restore — 사용자가 cancel 토글로 PASS 됐던 row 를 다시 LIKE 로 복원.
     *  - action_type = LIKE, expires_at = NULL (영구 제외 효과), tags_json 재freeze
     *  - SUPER_LIKE 였더라도 LIKE 로 복원 (단순화 — 별 환불 X)
     */
    public void changeToLike(String tagsJson) {
        this.actionType = MatchActionType.LIKE;
        this.expiresAt  = null;
        this.tagsJson   = tagsJson;
    }
}
