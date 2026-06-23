package com.nokcha.efbe.domain.chat.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_room_uuid", columnNames = "uuid"),
                @UniqueConstraint(name = "uk_chat_room_firebase_id", columnNames = "firebase_id")
        },
        indexes = {
                @Index(name = "idx_chat_room_pair", columnList = "pair_user_a_id, pair_user_b_id"),
                @Index(name = "idx_chat_room_type_post", columnList = "room_type, post_id"),
                @Index(name = "idx_chat_room_match", columnList = "match_result_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @Column(name = "firebase_id", nullable = false, length = 200)
    private String firebaseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "fk_chat_room_post"))
    private PostIt post;

    @Column(name = "post_content_snapshot", columnDefinition = "TEXT")
    private String postContentSnapshot;

    @Column(name = "power_message", columnDefinition = "TEXT")
    private String powerMessage;

    @Column(name = "power_pinned_until")
    private LocalDateTime powerPinnedUntil;

    @Column(name = "match_result_id")
    private Long matchResultId;

    @Column(name = "pair_user_a_id", nullable = false)
    private Long pairUserAId;

    @Column(name = "pair_user_b_id", nullable = false)
    private Long pairUserBId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDelete = Boolean.FALSE;

    @Column(name = "is_anonymous", nullable = false)
    private Boolean isAnonymous = Boolean.FALSE;

    @Builder
    private ChatRoom(String uuid, String firebaseId, ChatRoomType roomType, PostIt post, String postContentSnapshot, String powerMessage, LocalDateTime powerPinnedUntil, Long matchResultId, Long pairUserAId, Long pairUserBId, Boolean isActive, Boolean isDelete, Boolean isAnonymous) {
        this.uuid = uuid;
        this.firebaseId = firebaseId;
        this.roomType = roomType;
        this.post = post;
        this.postContentSnapshot = postContentSnapshot;
        this.powerMessage = powerMessage;
        this.powerPinnedUntil = powerPinnedUntil;
        this.matchResultId = matchResultId;
        this.pairUserAId = pairUserAId;
        this.pairUserBId = pairUserBId;
        this.isActive = isActive == null ? Boolean.TRUE : isActive;
        this.isDelete = Boolean.TRUE.equals(isDelete);
        this.isAnonymous = Boolean.TRUE.equals(isAnonymous);
    }

    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }

    public void activate() {
        this.isActive = Boolean.TRUE;
    }

    public void delete() {
        this.isDelete = Boolean.TRUE;
    }
}
