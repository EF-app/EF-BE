package com.nokcha.efbe.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.nokcha.efbe.domain.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class FcmNotificationSender {

    public void send(Notification notification) {
        String fcmToken = notification.getUser().getFcmToken();
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }

        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(notification.getTitle())
                        .setBody(notification.getBody())
                        .build())
                .putData("notificationId", String.valueOf(notification.getId()))
                .putData("type", notification.getType().name());

        if (notification.getTargetType() != null) {
            builder.putData("targetType", notification.getTargetType().name());
        }
        if (notification.getTargetId() != null) {
            builder.putData("targetId", String.valueOf(notification.getTargetId()));
        }
        if (notification.getDeepLink() != null && !notification.getDeepLink().isBlank()) {
            builder.putData("deepLink", notification.getDeepLink());
        }

        try {
            FirebaseMessaging.getInstance().send(builder.build());
            notification.markSent(LocalDateTime.now());
        } catch (FirebaseMessagingException e) {
            log.warn("[Notification] FCM 발송 실패 — notificationId={}, userId={}, err={}",
                    notification.getId(), notification.getUser().getId(), e.getMessage());
        } catch (RuntimeException e) {
            log.warn("[Notification] FCM 발송 준비 실패 — notificationId={}, userId={}, err={}",
                    notification.getId(), notification.getUser().getId(), e.getMessage());
        }
    }
}
