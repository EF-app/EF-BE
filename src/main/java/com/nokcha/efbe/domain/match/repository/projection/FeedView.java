package com.nokcha.efbe.domain.match.repository.projection;

import java.time.LocalDateTime;

/** 피드 한 행의 표시용 view (read-time 결과 + 카드 표시 데이터 + 거리 km + 마지막 활동 시각). */
public record FeedView(
        int matchRank,
        long targetId,
        String slotType,
        String tagsJson,
        String nickname,
        Integer age,
        String mbti,
        String job,
        String bioMessage,
        String country,
        String city,
        String mainPhotoUrl,
        Double distanceKm,
        LocalDateTime lastActiveAt
) {}
