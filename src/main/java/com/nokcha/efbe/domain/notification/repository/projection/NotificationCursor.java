package com.nokcha.efbe.domain.notification.repository.projection;

import java.time.LocalDateTime;

public record NotificationCursor(
        LocalDateTime sortAt,
        Long id
) { }