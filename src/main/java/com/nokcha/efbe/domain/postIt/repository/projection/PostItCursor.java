package com.nokcha.efbe.domain.postIt.repository.projection;

import java.time.LocalDateTime;

// 포스트잇 피드 커서 (createTime DESC, id DESC 정렬용)
public record PostItCursor(LocalDateTime createTime, Long id) {}
