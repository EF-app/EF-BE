package com.nokcha.efbe.domain.match.repository.projection;

import java.time.LocalDateTime;

/**
 * sent/received 좋아요 목록 raw row.
 *  searchSent: userId = ma.target_id (받은 사람)
 *  searchReceived: userId = ma.actor_id (보낸 사람)
 */
public record LikeActionRow(
        long actionId,
        String actionType,
        LocalDateTime createdAt,
        String tagsJson,
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
