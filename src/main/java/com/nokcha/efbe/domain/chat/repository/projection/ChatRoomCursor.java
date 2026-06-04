package com.nokcha.efbe.domain.chat.repository.projection;

import java.time.LocalDateTime;

public record ChatRoomCursor(LocalDateTime sortAt, Long id) {
}
