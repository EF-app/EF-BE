package com.nokcha.efbe.domain.match.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 본인 피드 재계산 throttle + 동시성 보호 (Phase 3) + 메모리 cleanup (Phase 4).
 *
 *  - 같은 userId 에 대한 재계산이 {@link #THROTTLE_MS} 안에 반복 호출되면 두 번째부터 skip
 *  - {@code ConcurrentMap.compute} 로 atomic CAS — 두 thread 가 동시 진입해도 1 회만 통과
 *  - {@link #cleanup()} 이 주기적으로 만료 entry 제거 → 가입자 수 증가에도 메모리 안정
 *
 *  단일 인스턴스 가정. 멀티 인스턴스 운영 시 Redis SETNX 또는 ShedLock 필요.
 *  ※ throttle 통과 자체가 "실행 권한" 이라 별도 ReentrantLock 불필요.
 */
@Slf4j
@Component
public class FeedRecomputeThrottle {

    /** 마지막 시작 후 30 초 이내 재호출은 skip. */
    static final long THROTTLE_MS = 30_000L;
    /** cleanup 보존 기간 — throttle 기간보다 충분히 길게 잡아 race 회피. */
    static final long CLEANUP_RETAIN_MS = 5 * 60 * 1000L;          // 5 분
    /** cleanup 실행 주기. */
    private static final long CLEANUP_INTERVAL_MS = 5 * 60 * 1000L; // 5 분

    private final ConcurrentMap<Long, Long> lastStartedAt = new ConcurrentHashMap<>();

    /**
     * 재계산 시작 권한 획득.
     *  - 통과: true 반환 + 시작 시각 갱신
     *  - 거부: false (이전 시작 후 THROTTLE_MS 미만)
     *  동시 호출 2개도 atomic CAS 로 1 회만 통과.
     */
    public boolean tryAcquire(long userId) {
        long now = System.currentTimeMillis();
        AtomicBoolean acquired = new AtomicBoolean(false);
        lastStartedAt.compute(userId, (k, prev) -> {
            if (prev == null || now - prev >= THROTTLE_MS) {
                acquired.set(true);
                return now;
            }
            return prev;
        });
        return acquired.get();
    }

    /**
     * 만료 entry 제거 — CLEANUP_RETAIN_MS 보다 오래된 entry 만 삭제.
     *  throttle 활성 기간 (THROTTLE_MS) 의 10 배 보존이라 동작에 영향 X — 단순 메모리 회수.
     */
    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void cleanup() {
        long threshold = System.currentTimeMillis() - CLEANUP_RETAIN_MS;
        int before = lastStartedAt.size();
        lastStartedAt.entrySet().removeIf(e -> e.getValue() < threshold);
        int after = lastStartedAt.size();
        if (before != after) {
            log.debug("[FeedRecomputeThrottle] cleanup — {}→{} entries", before, after);
        }
    }

    /** 테스트 가시성 — 패키지 가시성. */
    int size() {
        return lastStartedAt.size();
    }
}
