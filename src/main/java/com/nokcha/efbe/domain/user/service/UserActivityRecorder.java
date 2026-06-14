package com.nokcha.efbe.domain.user.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * users.last_active_at = NOW() 비동기 UPDATE 실행.
 *  호출처:
 *    - {@code UserActivityInterceptor.postHandle}  — throttle 통과 시 submit
 *    - {@code UserActivityController.heartbeat}    — FE 명시 heartbeat (throttle 우회)
 *
 *  설계:
 *    - @Async("userActivityExecutor") — 호출자 thread 영향 X
 *    - @Transactional(REQUIRES_NEW)   — HTTP request 트랜잭션 무관, 짧은 자체 commit
 *    - native SQL                     — entity load + dirty checking 우회 (1 round-trip 1ms)
 *    - 실패 시 throw X                — 다음 request 가 자연 흡수
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityRecorder {

    private final EntityManager em;

    @Async("userActivityExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void touch(long userId) {
        try {
            em.createNativeQuery("UPDATE users SET last_active_at = NOW() WHERE id = :id")
                    .setParameter("id", userId)
                    .executeUpdate();
        } catch (Exception e) {
            log.debug("[UserActivity] last_active_at 갱신 실패 — userId={}, err={}", userId, e.getMessage());
        }
    }
}
