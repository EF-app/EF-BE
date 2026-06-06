package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.user.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 가입 완료 commit 후 ColdStartFeed 트리거.
 *  - AFTER_COMMIT: 가입 트랜잭션이 성공적으로 commit 된 다음에만 실행 → 매칭 실패가 가입을 롤백하지 않음
 *  - 실패해도 가입은 이미 완료된 상태 → 실패 로그만 남기고 다음날 정상 배치에서 자연 회복
 *
 *  ColdStartFeed.build 가 자체 @Transactional 을 시작.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ColdStartFeedListener {

    private final UserManagement userManagement;
    private final ColdStartFeed coldStartFeed;
    private final MatchingConfigLoader configLoader;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCreated(UserCreatedEvent event) {
        try {
            UserContext me = userManagement.loadContext(event.userId());
            if (me == null) {
                log.warn("[ColdStartFeedListener] UserContext 로드 실패 — userId={}", event.userId());
                return;
            }
            coldStartFeed.build(me, configLoader.load());
        } catch (Exception e) {
            log.warn("[ColdStartFeedListener] 임시 피드 생성 실패 — userId={}, err={}",
                    event.userId(), e.getMessage(), e);
        }
    }
}
