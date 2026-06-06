package com.nokcha.efbe.domain.match.repository;

/**
 * 통합 지점 3 — 키워드 전역 보유자 수 (희귀도 = 보유자 수).
 *  배치가 갱신한 캐시를 조회. 없으면 0 = 가장 희귀.
 *
 *  구현체: Stage 5 의 {@code KeywordFreqServiceImpl}
 *   (user_keyword + user_custom_keyword GROUP BY 카운트 + Caffeine 캐시).
 *
 *  사용처: MatchCalculator 의 공통 키워드 칩 정렬 (빈도 낮은 것부터 N개).
 */
public interface KeywordFreqService {
    int countOf(String keyword);
}
