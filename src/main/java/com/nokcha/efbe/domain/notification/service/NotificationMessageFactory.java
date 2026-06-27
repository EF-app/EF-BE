package com.nokcha.efbe.domain.notification.service;

import com.nokcha.efbe.domain.notification.entity.NotificationType;
import org.springframework.stereotype.Component;

@Component
public class NotificationMessageFactory {

    public NotificationMessage create(NotificationType type, String subject) {
        return switch (type) {
            case NOTICE -> new NotificationMessage("[공지사항]", defaultIfBlank(subject, "새로운 공지사항을 확인해보세요."));
            case DAILY_MATCH -> new NotificationMessage("[오늘의 매칭]", "오늘의 매칭이 업데이트 됐어요.");
            case DAILY_BAL_GAME -> new NotificationMessage("[밸런스 게임]", "오늘의 밸런스 게임에 참여해보세요.");
            case MATCH_LIKE -> new NotificationMessage("[좋아요 알림]", "상대가 회원님에게 좋아요를 보냈어요.");
            case MATCH_COMPLETED -> new NotificationMessage("[매칭 완료]", "새로운 매칭이 성사되었어요.");
        };
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public record NotificationMessage(String title, String body) { }
}
