package com.nokcha.efbe.domain.notification.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.util.CursorCodec;
import com.nokcha.efbe.domain.notification.dto.response.NotificationRspDto;
import com.nokcha.efbe.domain.notification.entity.Notification;
import com.nokcha.efbe.domain.notification.entity.NotificationTargetType;
import com.nokcha.efbe.domain.notification.entity.NotificationType;
import com.nokcha.efbe.domain.notification.repository.NotificationRepository;
import com.nokcha.efbe.domain.notification.repository.projection.NotificationCursor;
import com.nokcha.efbe.domain.policy.entity.PolicyType;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    // TODO: 이후에 deeplink 도메인 주소로 변경 필요

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CursorCodec cursorCodec;
    private final FcmNotificationSender fcmNotificationSender;
    private final NotificationMessageFactory notificationMessageFactory;

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationRspDto> getMyNotifications(Long userId, String cursor, Integer size) {
        int pageSize = clampSize(size);
        NotificationCursor decoded = cursorCodec.decode(cursor, NotificationCursor.class);
        validateCursor(decoded);

        List<Notification> rows = decoded == null
                ? notificationRepository.findMyNotifications(userId, PageRequest.of(0, pageSize + 1))
                : notificationRepository.findMyNotificationsAfterCursor(userId, decoded.sortAt(), decoded.id(), PageRequest.of(0, pageSize + 1));

        boolean hasMore = rows.size() > pageSize;
        List<Notification> page = hasMore ? rows.subList(0, pageSize) : rows;
        List<NotificationRspDto> items = page.stream()
                .map(NotificationRspDto::from)
                .toList();

        if (!hasMore) return CursorPageResponse.last(items);

        Notification tail = page.getLast();
        String nextCursor = cursorCodec.encode(new NotificationCursor(tail.getCreateTime(), tail.getId()));
        return CursorPageResponse.of(items, nextCursor);
    }

    // 알림 단건 읽음 처리
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser_IdAndIsDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTIFICATION));
        notification.markAsRead(LocalDateTime.now());
    }

    // 알림 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    // 알림 삭제
    @Transactional
    public void delete(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUser_IdAndIsDeletedFalse(notificationId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTIFICATION));
        notification.delete(LocalDateTime.now());
    }

    @Transactional
    public void sendNoticeNotification(Long noticeId, String noticeTitle) {
        NotificationMessageFactory.NotificationMessage message = notificationMessageFactory.create(NotificationType.NOTICE, noticeTitle);
        sendToUsers(
                userRepository.findActiveUsersAgreedToPolicy(PolicyType.PUSH_AGREE),
                NotificationType.NOTICE,
                message.title(),
                message.body(),
                NotificationTargetType.NOTICE,
                noticeId,
                noticeId == null ? null : "/notice/" + noticeId
        );
    }

    @Transactional
    public void sendDailyMatchNotification(Collection<Long> userIds) {
        NotificationMessageFactory.NotificationMessage message = notificationMessageFactory.create(NotificationType.DAILY_MATCH, null);
        sendDailyToUsers(
                loadReceivers(userIds),
                NotificationType.DAILY_MATCH,
                message.title(),
                message.body(),
                NotificationTargetType.DAILY_MATCH,
                null,
                "/matches/daily"
        );
    }

    @Transactional
    public void sendDailyBalGameNotification(Collection<Long> userIds, Long balGameId) {
        NotificationMessageFactory.NotificationMessage message = notificationMessageFactory.create(NotificationType.DAILY_BAL_GAME, null);
        sendDailyToUsers(
                loadReceivers(userIds),
                NotificationType.DAILY_BAL_GAME,
                message.title(),
                message.body(),
                NotificationTargetType.BAL_GAME,
                balGameId,
                balGameId == null ? "/bal-games" : "/bal-games/" + balGameId
        );
    }

    @Transactional
    public void sendDailyBalGameNotificationToActiveUsers(Long balGameId) {
        sendDailyBalGameNotification(
                userRepository.findActiveUsersAgreedToPolicy(PolicyType.PUSH_AGREE).stream()
                        .map(User::getId)
                        .toList(),
                balGameId
        );
    }

    @Transactional
    public void sendMatchLikeNotification(Long receiverId, Long senderId) {
        User receiver = loadActiveReceiver(receiverId);
        if (receiver == null) return;

        NotificationMessageFactory.NotificationMessage message = notificationMessageFactory.create(NotificationType.MATCH_LIKE, null);
        Notification notification = saveNotification(
                receiver,
                NotificationType.MATCH_LIKE,
                message.title(),
                message.body(),
                NotificationTargetType.USER_PROFILE,
                senderId,
                senderId == null ? null : "/profile/" + senderId
        );
        fcmNotificationSender.send(notification);
    }

    @Transactional
    public void sendMatchCompletedNotification(Long firstUserId, Long secondUserId, Long matchResultId) {
        NotificationMessageFactory.NotificationMessage message = notificationMessageFactory.create(NotificationType.MATCH_COMPLETED, null);
        List<User> receivers = loadReceivers(List.of(firstUserId, secondUserId));
        sendToUsers(
                receivers,
                NotificationType.MATCH_COMPLETED,
                message.title(),
                message.body(),
                NotificationTargetType.MATCH_RESULT,
                matchResultId,
                matchResultId == null ? "/matches/mutual" : "/matches/mutual/" + matchResultId
        );
    }

    private void sendToUsers(List<User> users, NotificationType type, String title, String body,
                             NotificationTargetType targetType, Long targetId, String deepLink) {
        for (User user : users) {
            Notification notification = saveNotification(user, type, title, body, targetType, targetId, deepLink);
            fcmNotificationSender.send(notification);
        }
    }

    private void sendDailyToUsers(List<User> users, NotificationType type, String title, String body,
                                  NotificationTargetType targetType, Long targetId, String deepLink) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        for (User user : users) {
            if (notificationRepository.existsByUser_IdAndTypeAndCreateTimeBetween(
                    user.getId(), type, startOfDay, endOfDay)) {
                continue;
            }

            Notification notification = saveNotification(user, type, title, body, targetType, targetId, deepLink);
            fcmNotificationSender.send(notification);
        }
    }

    private Notification saveNotification(User user, NotificationType type, String title, String body,
                                          NotificationTargetType targetType, Long targetId, String deepLink) {
        return notificationRepository.save(Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .targetType(targetType)
                .targetId(targetId)
                .deepLink(deepLink)
                .build());
    }

    private User loadActiveReceiver(Long userId) {
        if (userId == null) return null;
        List<User> users = loadReceivers(List.of(userId));
        return users.isEmpty() ? null : users.getFirst();
    }

    private List<User> loadReceivers(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return userRepository.findActiveUsersByIdsAndPolicy(userIds, PolicyType.PUSH_AGREE);
    }

    private int clampSize(Integer size) {
        if (size == null || size <= 0) return DEFAULT_SIZE;
        if (size > MAX_SIZE) throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        return size;
    }

    private void validateCursor(NotificationCursor cursor) {
        if (cursor != null && (cursor.sortAt() == null || cursor.id() == null)) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }
}
