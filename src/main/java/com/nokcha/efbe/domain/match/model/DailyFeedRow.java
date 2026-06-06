package com.nokcha.efbe.domain.match.model;

/**
 * match_daily_feed 한 row 의 DTO. 슬롯 선정 결과 (FeedSelector 산출).
 *
 * @param rank      1~50 노출 순서
 * @param targetId  상대 user id
 * @param sortKey   뷰어 개인화 정렬값
 * @param slotType  SCORE / NEWBIE / RANDOM / CUSTOM_KW
 * @param tagsJson  표시 태그 JSON (TagDisplayFormatter 산출)
 */
public record DailyFeedRow(
        int rank,
        long targetId,
        double sortKey,
        String slotType,
        String tagsJson
) {}
