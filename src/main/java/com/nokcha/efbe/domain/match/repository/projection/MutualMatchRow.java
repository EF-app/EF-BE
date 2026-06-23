package com.nokcha.efbe.domain.match.repository.projection;

import java.time.LocalDateTime;

/** 서로 좋아요 목록 raw row — match_results + users JOIN. */
public record MutualMatchRow(
        long matchId,
        LocalDateTime matchedAt,
        String tagsJson,
        boolean isSuper,
        Long chatRoomId,
        long userId,
        String nickname,
        Integer age,
        LocalDateTime lastActiveAt,
        String country,
        String city,
        String mainPhotoUrl,
        String bioMessage,
        Double distanceKm
) {}
