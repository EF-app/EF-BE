package com.nokcha.efbe.domain.chat.entity;

import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "chat_participant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_participant_room_user", columnNames = {"chat_room_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_chat_participant_user_left", columnList = "user_id, left_at"),
                @Index(name = "idx_chat_participant_room", columnList = "chat_room_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_participant_room"))
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_chat_participant_user"))
    private User user;

    @Column(name = "display_name", nullable = false, length = 30)
    private String displayName; // 닉네임 스냅샷

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    @Column(name = "memo", length = 40)
    private String memo;

    @Column(name = "profile_open_level", nullable = false,
            columnDefinition = "TINYINT NOT NULL DEFAULT 1")
    private Integer profileOpenLevel = 1;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Builder
    private ChatParticipant(ChatRoom chatRoom, User user, String displayName) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.displayName = displayName;
        this.profileOpenLevel = 1;
    }

    public void leave(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public void advanceProfileOpenLevel() {
        if (profileOpenLevel == null) {
            profileOpenLevel = 1;
        }
        if (profileOpenLevel < 4) {
            profileOpenLevel++;
        }
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void rejoin() {
        this.leftAt = null;
    }

    public boolean hasLeft() {
        return leftAt != null;
    }
}
