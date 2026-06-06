package com.nokcha.efbe.domain.match.feed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedRecomputeThrottleTest {

    private final FeedRecomputeThrottle throttle = new FeedRecomputeThrottle();

    @Test
    @DisplayName("첫 호출은 통과, throttle 안에 두 번째 호출은 거부")
    void secondCallWithinWindowRejected() {
        assertTrue(throttle.tryAcquire(1L));
        assertFalse(throttle.tryAcquire(1L));
    }

    @Test
    @DisplayName("다른 userId 는 서로 영향 X")
    void differentUsersIndependent() {
        assertTrue(throttle.tryAcquire(1L));
        assertTrue(throttle.tryAcquire(2L));
        assertFalse(throttle.tryAcquire(1L));
        assertFalse(throttle.tryAcquire(2L));
    }

    @Test
    @DisplayName("cleanup — CLEANUP_RETAIN_MS 보다 오래된 entry 만 제거")
    void cleanupRemovesExpiredEntries() throws Exception {
        ConcurrentMap<Long, Long> map = mapField(throttle);
        long now = System.currentTimeMillis();
        map.put(100L, now);                                                       // 활성
        map.put(101L, now - FeedRecomputeThrottle.CLEANUP_RETAIN_MS - 1_000L);    // 만료

        throttle.cleanup();

        assertEquals(1, throttle.size(), "만료된 1개만 제거");
        assertTrue(map.containsKey(100L));
        assertFalse(map.containsKey(101L));
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentMap<Long, Long> mapField(FeedRecomputeThrottle t) throws Exception {
        Field f = FeedRecomputeThrottle.class.getDeclaredField("lastStartedAt");
        f.setAccessible(true);
        return (ConcurrentMap<Long, Long>) f.get(t);
    }

    @Test
    @DisplayName("같은 userId 동시 호출 N 개 → 정확히 1 회만 통과")
    void concurrentCallsOnlyOneAcquires() throws Exception {
        int threads = 32;
        ExecutorService es = Executors.newFixedThreadPool(threads);
        AtomicInteger acquired = new AtomicInteger();
        Future<?>[] futures = new Future[threads];
        for (int i = 0; i < threads; i++) {
            futures[i] = es.submit(() -> {
                if (throttle.tryAcquire(99L)) acquired.incrementAndGet();
            });
        }
        for (Future<?> f : futures) f.get(5, TimeUnit.SECONDS);
        es.shutdown();
        assertEquals(1, acquired.get(), "동시 32개 호출 중 정확히 1개만 통과");
    }
}
