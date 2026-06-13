package com.nokcha.efbe.domain.chat.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.Timestamp;
import com.google.firebase.cloud.FirestoreClient;
import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.chat.dto.firebase.ChatFirebaseMessageSnapshot;
import com.nokcha.efbe.domain.chat.entity.ChatReportMessageType;
import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ChatFirebaseService {

    @Value("${firebase.firestore.chat-room-collection:chatRooms}")
    private String chatRoomCollection;

    @Value("${firebase.firestore.message-subcollection:messages}")
    private String messageSubcollection;

    public String generateRoomDocumentId() {
        return firestore().collection(chatRoomCollection)
                .document()
                .getId();
    }

    public void createRoomDocument(ChatRoom room, List<Long> participantIds) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("chatRoomId", room.getId());
        data.put("roomType", room.getRoomType().name());
        data.put("postId", room.getPost() == null ? null : room.getPost().getId());
        data.put("matchResultId", room.getMatchResultId());
        data.put("pairUserAId", room.getPairUserAId());
        data.put("pairUserBId", room.getPairUserBId());
        data.put("participantIds", participantIds);
        data.put("isActive", Boolean.TRUE.equals(room.getIsActive()));
        data.put("isDelete", Boolean.TRUE.equals(room.getIsDelete()));
        data.put("isAnonymous", Boolean.TRUE.equals(room.getIsAnonymous()));
        data.put("postContentSnapshot", room.getPostContentSnapshot());
        data.put("powerMessage", room.getPowerMessage());
        data.put("powerPinnedUntil", room.getPowerPinnedUntil() == null ? null : room.getPowerPinnedUntil().toString());
        data.put("lastMessage", room.getLastMessage());
        data.put("lastMessageAt", room.getLastMessageAt() == null ? null : room.getLastMessageAt().toString());
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        try {
            DocumentReference document = firestore()
                    .collection(chatRoomCollection)
                    .document(room.getFirebaseId());
            document.set(data).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIREBASE_CHAT_ROOM_CREATE_FAILED, e);
        } catch (ExecutionException e) {
            throw new BusinessException(ErrorCode.FIREBASE_CHAT_ROOM_CREATE_FAILED, e);
        }
    }

    public ChatFirebaseMessageSnapshot readMessage(ChatRoom room, String firebaseMessageId) {
        try {
            DocumentSnapshot document = firestore()
                    .collection(chatRoomCollection)
                    .document(room.getFirebaseId())
                    .collection(messageSubcollection)
                    .document(firebaseMessageId)
                    .get()
                    .get();

            if (!document.exists()) {
                throw new BusinessException(ErrorCode.NOT_FOUND_CHAT_MESSAGE);
            }

            return new ChatFirebaseMessageSnapshot(
                    document.getId(),
                    resolveMessageType(document),
                    getLong(document, "senderUserId", "userId", "senderId"),
                    getString(document, "contentSnapshot", "content", "text", "message"),
                    getString(document, "imageStoragePath", "storagePath", "imagePath"),
                    getString(document, "imageUrlSnapshot", "imageUrl", "imageURL", "url"),
                    getString(document, "mimeType", "contentType"),
                    getLocalDateTime(document, "sentAt", "createdAt", "timestamp")
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.FIREBASE_CHAT_MESSAGE_READ_FAILED, e);
        } catch (ExecutionException e) {
            throw new BusinessException(ErrorCode.FIREBASE_CHAT_MESSAGE_READ_FAILED, e);
        }
    }

    private ChatReportMessageType resolveMessageType(DocumentSnapshot document) {
        String type = getString(document, "messageType", "type");
        if (type != null) {
            try {
                return ChatReportMessageType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // 이미지 필드 존재 여부로 다시 추론한다.
            }
        }
        if (getString(document, "imageStoragePath", "storagePath", "imagePath", "imageUrl", "imageURL", "url") != null) {
            return ChatReportMessageType.IMAGE;
        }
        return ChatReportMessageType.TEXT;
    }

    private String getString(DocumentSnapshot document, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = document.get(fieldName);
            if (value instanceof String stringValue && !stringValue.isBlank()) {
                return stringValue.trim();
            }
        }
        return null;
    }

    private Long getLong(DocumentSnapshot document, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = document.get(fieldName);
            if (value instanceof Number numberValue) {
                return numberValue.longValue();
            }
            if (value instanceof String stringValue && !stringValue.isBlank()) {
                try {
                    return Long.parseLong(stringValue.trim());
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private LocalDateTime getLocalDateTime(DocumentSnapshot document, String... fieldNames) {
        for (String fieldName : fieldNames) {
            Object value = document.get(fieldName);
            LocalDateTime parsed = parseLocalDateTime(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDateTime parseLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return LocalDateTime.ofInstant(timestamp.toDate().toInstant(), java.time.ZoneId.systemDefault());
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        }
        if (value instanceof Number numberValue) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(numberValue.longValue()), java.time.ZoneId.systemDefault());
        }
        if (value instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return LocalDateTime.parse(stringValue.trim());
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}
