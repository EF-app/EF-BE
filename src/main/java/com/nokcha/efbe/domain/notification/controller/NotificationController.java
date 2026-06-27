package com.nokcha.efbe.domain.notification.controller;

import com.nokcha.efbe.common.response.CursorPageResponse;
import com.nokcha.efbe.common.response.RspTemplate;
import com.nokcha.efbe.common.util.SecurityUtil;
import com.nokcha.efbe.domain.notification.dto.response.NotificationRspDto;
import com.nokcha.efbe.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    @Operation(summary = "내 알림 목록", description = "채팅 알림을 제외한 내 알림 목록을 커서 기반으로 조회합니다.")
    @GetMapping
    public RspTemplate<CursorPageResponse<NotificationRspDto>> getMyNotifications(
            @Parameter(description = "이전 응답의 nextCursor. 첫 페이지는 비워서 호출") @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수. 기본 20, 최대 50") @RequestParam(required = false) Integer size) {
        Long userId = securityUtil.getCurrentUserId();
        CursorPageResponse<NotificationRspDto> data = notificationService.getMyNotifications(userId, cursor, size);
        return new RspTemplate<>(HttpStatus.OK, "알림 목록 조회 성공", data);
    }

    @Operation(summary = "알림 단일 읽음 처리")
    @PatchMapping("/{notificationId}/read")
    public RspTemplate<Void> markAsRead(@PathVariable Long notificationId) {
        Long userId = securityUtil.getCurrentUserId();
        notificationService.markAsRead(userId, notificationId);
        return new RspTemplate<>(HttpStatus.OK, "알림 읽음 처리 성공");
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "채팅 알림을 제외한 내 알림을 모두 읽음 처리합니다.")
    @PatchMapping("/read-all")
    public RspTemplate<Void> markAllAsRead() {
        Long userId = securityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return new RspTemplate<>(HttpStatus.OK, "알림 전체 읽음 처리 성공");
    }

    @Operation(summary = "알림 삭제", description = "알림을 사용자 목록에서 삭제 처리합니다.")
    @DeleteMapping("/{notificationId}")
    public RspTemplate<Void> delete(@PathVariable Long notificationId) {
        Long userId = securityUtil.getCurrentUserId();
        notificationService.delete(userId, notificationId);
        return new RspTemplate<>(HttpStatus.OK, "알림 삭제 성공");
    }
}
