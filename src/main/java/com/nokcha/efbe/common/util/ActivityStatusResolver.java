package com.nokcha.efbe.common.util;

import com.nokcha.efbe.domain.user.model.ActivityStatus;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * {@code users.last_active_at} → {@link ActivityStatus} 접속 상태 계산.
 */
public final class ActivityStatusResolver {

    private static final long NOW_MIN = 10;
    private static final long RECENT_MIN = 60;
    private static final long TODAY_MIN = 1_440;

    private ActivityStatusResolver() {}

    public static ActivityStatus resolve(LocalDateTime lastActiveAt) {
        if (lastActiveAt == null) return ActivityStatus.OLDER;
        long minutes = Duration.between(lastActiveAt, LocalDateTime.now()).toMinutes();
        if (minutes <= NOW_MIN) return ActivityStatus.NOW;
        if (minutes <= RECENT_MIN) return ActivityStatus.RECENT;
        if (minutes <= TODAY_MIN) return ActivityStatus.TODAY;
        return ActivityStatus.OLDER;
    }
}
