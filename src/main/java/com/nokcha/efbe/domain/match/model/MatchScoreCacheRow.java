package com.nokcha.efbe.domain.match.model;

/**
 * match_score_cache 한 row 의 DTO (페어 = LEAST/GREATEST 컨벤션).
 *  - PairScore → MatchScoreCacheRow 변환은 영속 계층 (Stage 5) 에서 처리.
 *  - categoryMateCodesJson, hasCustomKeyword, hasTotalOpposite 는 캐시 상태에 저장
 *    (단건 "받은 좋아요" 카드 즉시 렌더링용).
 */
public record MatchScoreCacheRow(
        long userAId,
        long userBId,
        double keywordScore,
        double lifestyleScore,
        double locationScore,
        Double idealScore,
        Double idealAtoB,
        Double idealBtoA,
        String categoryMateCodesJson,
        boolean hasCustomKeyword,
        boolean hasTotalOpposite
) {}
