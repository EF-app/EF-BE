package com.nokcha.efbe.domain.postIt.repository.projection;

import java.time.LocalDateTime;

// "내가 반응한" 탭 커서 (post_it.create_time DESC, post_it.id DESC 정렬용)
public record UserActivityReactedPostItCursor(LocalDateTime createTime, Long id) {}
