package com.nokcha.efbe.domain.chat.dto.firebase;

import com.nokcha.efbe.domain.chat.entity.ChatReportMessageType;

import java.time.LocalDateTime;

public record ChatFirebaseMessageSnapshot(
        String firebaseMessageId,
        ChatReportMessageType messageType,
        Long senderUserId,
        String contentSnapshot,
        String imageStoragePath,
        String imageUrlSnapshot,
        String mimeType,
        LocalDateTime sentAt
) {
}
