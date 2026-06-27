package com.nokcha.efbe.domain.notification.repository;

import com.nokcha.efbe.domain.notification.entity.Notification;
import com.nokcha.efbe.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select n from Notification n " +
            "where n.user.id = :userId " +
            "and n.isDeleted = false " +
            "order by n.createTime desc, n.id desc")
    List<Notification> findMyNotifications(@Param("userId") Long userId,
                                           Pageable pageable);

    @Query("select n from Notification n " +
            "where n.user.id = :userId " +
            "and n.isDeleted = false " +
            "and (n.createTime < :cursorSortAt " +
            "or (n.createTime = :cursorSortAt and n.id < :cursorId)) " +
            "order by n.createTime desc, n.id desc")
    List<Notification> findMyNotificationsAfterCursor(@Param("userId") Long userId,
                                                      @Param("cursorSortAt") LocalDateTime cursorSortAt,
                                                      @Param("cursorId") Long cursorId,
                                                      Pageable pageable);

    Optional<Notification> findByIdAndUser_IdAndIsDeletedFalse(Long id, Long userId);

    boolean existsByUser_IdAndTypeAndCreateTimeBetween(Long userId,
                                                       NotificationType type,
                                                       LocalDateTime startAt,
                                                       LocalDateTime endAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notification n " +
            "set n.isRead = true, n.readAt = :readAt " +
            "where n.user.id = :userId " +
            "and n.isDeleted = false " +
            "and n.isRead = false")
    void markAllAsRead(@Param("userId") Long userId,
                       @Param("readAt") LocalDateTime readAt);
}
