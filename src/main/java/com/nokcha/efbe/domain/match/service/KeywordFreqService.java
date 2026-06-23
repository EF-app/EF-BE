package com.nokcha.efbe.domain.match.service;

import com.nokcha.efbe.domain.profile.repository.UserCustomKeywordRepository;
import com.nokcha.efbe.domain.profile.repository.UserKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 통합 지점 3 — 키워드 전역 보유자 수 (희귀도 = 보유자 수).
 *  배치가 갱신한 캐시를 조회. 없으면 0 = 가장 희귀.
 *
 *  - 부팅 직후 1회 자동 갱신은 {@link KeywordFreqInitializer} 가 ApplicationReadyEvent 로 트리거
 *    (별도 빈에서 외부 호출 → AOP proxy 통과 → @Transactional 정상 적용. strict 트랜잭션 모드 안전)
 *  - 배치 시작 시 {@link #refresh()} 1회 호출 → 캐시 갱신
 *  - {@link #countOf(String)} 은 캐시 lookup, 미스 시 0 (가장 희귀)
 *  - volatile Map reference swap — read 는 1회 volatile read, refresh 는 새 Map 으로 atomic 교체.
 *
 *  이 클래스는 캐시 / 동시성 / 트랜잭션 책임.
 *
 *  키 = small_category (예: "락"). 값 = user_keyword + user_custom_keyword 보유자 수 합.
 *  사용처: MatchCalculator 의 공통 키워드 칩 정렬 (빈도 낮은 것부터 N개).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordFreqService {

    private final UserKeywordRepository userKeywordRepo;
    private final UserCustomKeywordRepository userCustomKeywordRepo;

    /** atomic reference swap 으로 refresh 중 race window 차단. read 는 volatile read 한 번. */
    private volatile Map<String, Integer> cache = Map.of();

    public int countOf(String keyword) {
        if (keyword == null) return 0;
        return cache.getOrDefault(keyword, 0);
    }

    /** 배치 시작 시 호출 — 전체 키워드 빈도 적재. */
    @Transactional(readOnly = true)
    public void refresh() {
        Map<String, Integer> next = new HashMap<>();
        for (var row : userKeywordRepo.countByCodeKeyword()) {
            next.merge(row.label(), row.cnt().intValue(), Integer::sum);
        }
        for (var row : userCustomKeywordRepo.countByKeyword()) {
            next.merge(row.label(), row.cnt().intValue(), Integer::sum);
        }
        // ConcurrentHashMap clear+putAll 의 race window 회피 — 새 map 으로 atomic reference swap.
        this.cache = Map.copyOf(next);
        log.info("[KeywordFreqService] 캐시 갱신 — {} 개 키워드", cache.size());
    }
}
