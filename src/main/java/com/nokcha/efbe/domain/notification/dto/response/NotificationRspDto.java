package com.nokcha.efbe.domain.notification.dto.response;

import com.nokcha.efbe.domain.notification.entity.Notification;
import com.nokcha.efbe.domain.notification.entity.NotificationTargetType;
import com.nokcha.efbe.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "알림 응답")
public class NotificationRspDto {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 타입", example = "MATCH_LIKE")
    private NotificationType type;

    @Schema(description = "제목", example = "새로운 좋아요가 도착했어요")
    private String title;

    @Schema(description = "본문", example = "상대가 회원님에게 좋아요를 보냈어요.")
    private String body;

    @Schema(description = "클릭 시 이동 대상 타입", example = "USER_PROFILE", nullable = true)
    private NotificationTargetType targetType;

    @Schema(description = "클릭 시 이동 대상 ID", example = "10", nullable = true)
    private Long targetId;

    @Schema(description = "앱 딥링크", example = "/profile/10", nullable = true)
    private String deepLink;

    @Schema(description = "읽음 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "읽은 시각", example = "2026-06-23T10:00:00", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "생성 시각", example = "2026-06-23T09:30:00")
    private LocalDateTime createdAt;

    public static NotificationRspDto from(Notification notification) {
        return NotificationRspDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .deepLink(notification.getDeepLink())
                .isRead(Boolean.TRUE.equals(notification.getIsRead()))
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreateTime())
                .build();
    }
}
