package com.nokcha.efbe.domain.match.repository.projection;

/**
 * 키워드 빈도 집계 1행 — KeywordFreqService.refresh() 가 두 Repository 결과를 merge.
 *  label : code_keyword.small_category 또는 user_custom_keyword.keyword
 *  cnt   : 보유자 수 (COUNT(DISTINCT user_id))
 */
public record KeywordFreqProjection(String label, Long cnt) {}
