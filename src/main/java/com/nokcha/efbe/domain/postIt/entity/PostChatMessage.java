package com.nokcha.efbe.domain.postIt.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 포스트잇 채팅 메시지 엔티티 (post_chat_message, 취소 가능)
// [v1.6] uuid / [v1.7] message_type + quoted_post_id
@Getter
@Entity
@Table(name = "post_chat_message",
        uniqueConstraints = {@UniqueConstraint(name = "uk_pcm_uuid", columnNames = "uuid")},
        indexes = {@Index(name = "idx_msg_room_time", columnList = "room_id, create_time")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // [v1.6] 외부 API path 용 UUID
    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_msg_room"))
    private PostChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_msg_sender"))
    private User sender;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // [v1.7] 메시지 유형 (TEXT / SYSTEM / POST_QUOTE)
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 15)
    private PostMessageType messageType = PostMessageType.TEXT;

    // [v1.7] POST_QUOTE 시 원글 FK (다른 포스트잇 인용)
    @Column(name = "quoted_post_id")
    private Long quotedPostId;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = Boolean.FALSE;

    @Builder
    private PostChatMessage(String uuid, PostChatRoom room, User sender, String content,
                            PostMessageType messageType, Long quotedPostId) {
        this.uuid = uuid;
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.messageType = messageType == null ? PostMessageType.TEXT : messageType;
        this.quotedPostId = quotedPostId;
        this.isDeleted = Boolean.FALSE;
    }

    // 읽음 처리
    public void markRead() {
        if (this.readAt == null) this.readAt = LocalDateTime.now();
    }

    // 취소 (Soft delete) - read_at 없을 때만 허용 (외부에서 검증)
    public void cancel() {
        this.isDeleted = Boolean.TRUE;
    }
}
