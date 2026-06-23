package com.nokcha.efbe.domain.match.model;

import java.util.List;

/**
 * 한 페어 (me ↔ other) 계산 결과.
 *  - 슬롯 입력 (FeedSelector) 의 입력.
 *  - tags 는 뷰어 me 관점 (#내가/#나를 반전 완료).
 *  - sortKey 는 PairScore 에 포함되지 않음 — FeedSelector 가 정렬 시점에 직접 계산.
 *
 * @param otherId          상대 user id
 * @param keyword          키워드 점수 0~1 (관심사 + 개인키워드 union Jaccard)
 * @param idealBidir       이상형 양방향 평균 (#이상형 태그 표시값)
 * @param lifestyle        라이프 점수
 * @param location         지역 점수
 * @param aToB             me 이상형 ↔ other 실제 (비대칭 판정 보존)
 * @param bToA             other 이상형 ↔ me 실제
 * @param categoryMateCodes 발동 카테고리 코드 (예: ["OUTDOOR","SPORTS"])
 * @param hasCustomKeyword 개인키워드 공통 ≥ 1
 * @param hasTotalOpposite 정반대의매력 발동
 * @param newbie           other 가 뉴비인가?
 * @param tags             표시 태그 묶음 (관점 반영 완료)
 */
public record PairScore(
        long otherId,
        double keyword,
        double idealBidir,
        double lifestyle,
        double location,
        double aToB,
        double bToA,
        List<String> categoryMateCodes,
        boolean hasCustomKeyword,
        boolean hasTotalOpposite,
        boolean newbie,
        List<Tag> tags
) {}
