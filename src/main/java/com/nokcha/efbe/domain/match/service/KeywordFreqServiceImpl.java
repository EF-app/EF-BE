package com.nokcha.efbe.domain.match.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 통합 지점 3 실구현 — 키워드 전역 보유자 수 캐시.
 *  - 부팅 직후 1회 자동 갱신은 {@link KeywordFreqInitializer} 가 ApplicationReadyEvent 로 트리거
 *    (별도 빈에서 외부 호출 → AOP proxy 통과 → @Transactional 정상 적용. strict 트랜잭션 모드 안전)
 *  - 배치 시작 시 {@link #refresh()} 1회 호출 → 캐시 갱신
 *  - {@link #countOf(String)} 은 캐시 lookup, 미스 시 0 (가장 희귀)
 *  - volatile Map reference swap — read 는 1회 volatile read, refresh 는 새 Map 으로 atomic 교체.
 *
 *  키 = small_category (예: "락"). 값 = user_keyword + user_custom_keyword 보유자 수 합.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordFreqServiceImpl implements KeywordFreqService {

    private final EntityManager em;

    /** atomic reference swap 으로 refresh 중 race window 차단. read 는 volatile read 한 번. */
    private volatile Map<String, Integer> cache = Map.of();

    @Override
    public int countOf(String keyword) {
        if (keyword == null) return 0;
        return cache.getOrDefault(keyword, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public void refresh() {
        Map<String, Integer> next = new HashMap<>();

        /* code_keyword.small_category 기준 보유자 수 */
        @SuppressWarnings("unchecked")
        List<Object[]> ckRows = em.createNativeQuery("""
                SELECT ck.small_category, COUNT(DISTINCT uk.user_id)
                  FROM user_keyword uk
                  JOIN code_keyword ck ON ck.id = uk.keyword_id
                 GROUP BY ck.small_category
                """).getResultList();
        for (Object[] r : ckRows) {
            String label = (String) r[0];
            int count = ((Number) r[1]).intValue();
            next.merge(label, count, Integer::sum);
        }

        /* user_custom_keyword.keyword 기준 보유자 수 */
        @SuppressWarnings("unchecked")
        List<Object[]> uckRows = em.createNativeQuery("""
                SELECT uck.keyword, COUNT(DISTINCT uck.user_id)
                  FROM user_custom_keyword uck
                 GROUP BY uck.keyword
                """).getResultList();
        for (Object[] r : uckRows) {
            String label = (String) r[0];
            int count = ((Number) r[1]).intValue();
            next.merge(label, count, Integer::sum);
        }

        // ConcurrentHashMap clear+putAll 의 race window 회피 — 새 map 으로 atomic reference swap.
        this.cache = Map.copyOf(next);
        log.info("[KeywordFreqService] 캐시 갱신 — {} 개 키워드", cache.size());
    }
}
