package com.nokcha.efbe.domain.balGame.repository.projection;

import java.time.LocalDateTime;

// 밸런스 게임 피드 커서 (createTime DESC, id DESC 정렬용)
public record BalGameCursor(LocalDateTime createTime, Long id) {}
