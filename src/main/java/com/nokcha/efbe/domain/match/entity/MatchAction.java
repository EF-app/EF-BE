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
 *  한 페어(actor → target) 당 활성 액션 1개 정책 — 변경 시 DELETE + INSERT.
 *
 *  expires_at:
 *    - PASS         → NOW() + cfg.passCooldownDays
 *    - 그 외        → null (영구 제외)
 *
 *  tagsJson (LIKE 류만 채움):
 *    - 액션 시점 actor 관점 매칭 태그 freeze (MatchCalculator + TagDisplayFormatter)
 *    - 카드 표시 — "내가 누른 좋아요" 는 그대로, "받은 좋아요" 는 표시 시점 #내가/#나를 반전
 *    - PASS 는 null (카드 노출 X)
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
}
