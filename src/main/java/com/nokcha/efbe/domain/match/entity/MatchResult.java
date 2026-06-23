package com.nokcha.efbe.domain.match.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.match.model.MatchTriggerType;
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

/**
 * match_results — 매칭 성사 결과
 *  페어 (LEAST userAId, GREATEST userBId) 정규화 — UNIQUE 페어 1 row 보장.
 *  cancel/restore 흐름은 match_actions UPDATE 만, match_results 는 row 유지.
 *  is_super = 양쪽 중 SUPER_LIKE 하나라도.
 */
@Getter
@Entity
@Table(
        name = "match_results",
        uniqueConstraints = @UniqueConstraint(name = "uk_match_pair", columnNames = {"user_a_id", "user_b_id"}),
        indexes = {
                @Index(name = "idx_match_user_a", columnList = "user_a_id, create_time DESC"),
                @Index(name = "idx_match_user_b", columnList = "user_b_id, create_time DESC"),
                @Index(name = "idx_match_trigger", columnList = "trigger_type, create_time DESC"),
                @Index(name = "idx_match_chat_room", columnList = "chat_room_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** LEAST(actor, target). 페어 정규화로 같은 페어가 두 row 안 생기게 보장. */
    @Column(name = "user_a_id", nullable = false)
    private Long userAId;

    /** GREATEST(actor, target). */
    @Column(name = "user_b_id", nullable = false)
    private Long userBId;

    /** chat — 첫 메시지 전송 시 채워짐. */
    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private MatchTriggerType triggerType;

    /** 양쪽 중 SUPER_LIKE 하나라도. mutual 카드 차등 표시용. */
    @Column(name = "is_super", nullable = false)
    private boolean isSuper;

    @Builder
    private MatchResult(Long userAId, Long userBId, MatchTriggerType triggerType, boolean isSuper) {
        this.userAId = userAId;
        this.userBId = userBId;
        this.triggerType = triggerType;
        this.isSuper = isSuper;
    }

    public void assignChatRoom(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }
}
