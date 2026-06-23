package com.nokcha.efbe.domain.chat.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.report.entity.Report;
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

@Getter
@Entity
@Table(name = "chat_report_evidence",
        indexes = {
                @Index(name = "idx_chat_report_evidence_report", columnList = "report_id"),
                @Index(name = "idx_chat_report_evidence_room", columnList = "chat_room_id")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatReportEvidence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_report_evidence_report"))
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chat_room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chat_report_evidence_room"))
    private ChatRoom chatRoom;

    @Column(name = "firebase_message_id", length = 200)
    private String firebaseMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private ChatReportMessageType messageType;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "content_snapshot", columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "image_storage_path", length = 500)
    private String imageStoragePath;

    @Column(name = "image_url_snapshot", length = 1000)
    private String imageUrlSnapshot;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Builder
    private ChatReportEvidence(Report report, ChatRoom chatRoom, String firebaseMessageId,
                               ChatReportMessageType messageType, Long senderUserId, String contentSnapshot,
                               String imageStoragePath, String imageUrlSnapshot, String mimeType,
                               LocalDateTime sentAt) {
        this.report = report;
        this.chatRoom = chatRoom;
        this.firebaseMessageId = firebaseMessageId;
        this.messageType = messageType == null ? ChatReportMessageType.TEXT : messageType;
        this.senderUserId = senderUserId;
        this.contentSnapshot = contentSnapshot;
        this.imageStoragePath = imageStoragePath;
        this.imageUrlSnapshot = imageUrlSnapshot;
        this.mimeType = mimeType;
        this.sentAt = sentAt;
    }
}
