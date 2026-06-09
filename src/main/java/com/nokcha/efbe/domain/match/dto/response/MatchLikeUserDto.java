package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 받은 좋아요 / 보낸 좋아요 카드의 상대방 mini 정보. */
@Schema(description = "좋아요 카드의 상대방 정보")
public record MatchLikeUserDto(
        @Schema(description = "user.id (string)") String id,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "나이") Integer age,
        @Schema(description = "지역 (country + city)") String region,
        @Schema(description = "tags_json 평탄화 (최대 6개)") List<String> tags,
        @Schema(description = "tags_json[0].percent 또는 0") Integer matchScore,
        @Schema(description = "last_active_at >= NOW - 10분") Boolean isOnline,
        @Schema(description = "user_profile_image sort_order=1") String mainPhotoUrl,
        @Schema(description = "프로필 소개 (user_profile.bio_message)") String bioMessage,
        @Schema(description = "viewer 와 상대방 사이 거리 km. 좌표 미설정 시 null") Double distanceKm
) {}
