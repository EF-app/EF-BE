package com.nokcha.efbe.domain.match.repository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 통합 지점 3 실구현 — 키워드 전역 보유자 수 캐시.
 *  - 부팅 직후 {@link #initOnBoot()} → {@link #refresh()} 1회 자동 호출 (캐시 미초기화 방지)
 *  - 배치 시작 시 {@link #refresh()} 1회 호출 → 캐시 갱신
 *  - {@link #countOf(String)} 은 캐시 lookup, 미스 시 0 (가장 희귀)
 *  - Caffeine TTL 도입 없이 단순 ConcurrentHashMap — 배치 1회/일 + 부팅 1회로 충분
 *
 *  키 = small_category (예: "락"). 값 = user_keyword + user_custom_keyword 보유자 수 합.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordFreqServiceImpl implements KeywordFreqService {

    private final EntityManager em;

    private final Map<String, Integer> cache = new ConcurrentHashMap<>();

    /**
     * 부팅 직후 1회 자동 호출 — JVM 재시작 후 ~ 다음 04:00 배치 사이 캐시 빈 상태 방지.
     *  실패해도 부팅을 막지 않음 (warn 로그 + 빈 캐시로 시작, 04:00 배치가 채움).
     */
    @PostConstruct
    public void initOnBoot() {
        try {
            refresh();
        } catch (Exception e) {
            log.warn("[KeywordFreqService] 부팅 시 캐시 초기화 실패 — 04:00 배치까지 빈 상태. err={}",
                    e.getMessage(), e);
        }
    }

    @Override
    public int countOf(String keyword) {
        if (keyword == null) return 0;
        return cache.getOrDefault(keyword, 0);
    }

    /** 배치 시작 시 호출. 한 번에 전체 키워드 빈도 적재. */
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

        cache.clear();
        cache.putAll(next);
        log.info("[KeywordFreqService] 캐시 갱신 — {} 개 키워드", cache.size());
    }
}
