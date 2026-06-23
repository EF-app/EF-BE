package com.nokcha.efbe.domain.user.service;

import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * users.last_active_at 비동기 갱신 실행.
 *  호출처:
 *    - {@code UserActivityInterceptor.postHandle}  — throttle 통과 시 submit
 *    - {@code UserActivityController.heartbeat}    — FE 명시 heartbeat (throttle 우회)
 *
 *  설계:
 *    - @Async("userActivityExecutor") — 호출자 thread 영향 X
 *    - @Transactional(REQUIRES_NEW)   — HTTP request 트랜잭션 무관, 짧은 자체 commit
 *    - JPQL bulk update               — repository 에 갱신 책임 위임
 *    - 실패 시 throw X                — 다음 request 가 자연 흡수
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserRepository userRepository;

    @Async("userActivityExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 2)
    public void touch(long userId) {
        try {
            userRepository.updateLastActiveAt(userId, LocalDateTime.now());
        } catch (Exception e) {
            log.debug("[UserActivity] last_active_at 갱신 실패 — userId={}, err={}", userId, e.getMessage());
        }
    }
}
