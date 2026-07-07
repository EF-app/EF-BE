package com.nokcha.efbe.domain.match.feed;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.config.MatchingConfigLoader;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.MatchActionRepository;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import com.nokcha.efbe.domain.profile.event.ProfileUpdatedEvent;
import com.nokcha.efbe.domain.user.event.UserCreatedEvent;
import com.nokcha.efbe.domain.user.event.UserReactivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

/**
 * 매칭 본인 피드 재계산 트리거 — 외부 도메인 이벤트 → 매칭 도메인 진입점 어댑터.
 *  단일 Listener 로 5개 진입점 통합 (이전 ColdStartFeedListener + ProfileChangeListener 폐기).
 *
 *  ── 5개 진입점 ────────────────────────────────────────────────────────
 *    (1) UserCreatedEvent       (가입)           → ColdStartFeed.build (인기/최근 풀)
 *    (2) ProfileUpdatedEvent    (지역 수정)      → MyFeedRecomputer.recompute (어뷰즈 가드)
 *    (3) UserReactivatedEvent (DORMANT_RECOVERED)  → MyFeedRecomputer.recompute
 *    (4) UserReactivatedEvent (WITHDRAW_CANCELLED) → MyFeedRecomputer.recompute
 *    (5) UserReactivatedEvent (SUSPENSION_LIFTED)  → MyFeedRecomputer.recompute
 *
 *  ── 공통 정책 ─────────────────────────────────────────────────────────
 *    - AFTER_COMMIT : 외부 도메인 트랜잭션 commit 후에만 실행 (외부 롤백 시 매칭 실행 X)
 *    - @Async       : 사용자 응답 thread 안 막음 (재계산 = 후보 풀 500 + 점수 → 수백 ms)
 *    - try/catch    : 실패 시 warn 로그만 — 다음 04:00 배치 / 05:00 보정에서 자연 회복
 *
 *  ── 어뷰즈 가드 — 프로필 변경만 적용 ──────────────────────────
 *    - throttle 30초 atomic CAS (FeedRecomputeThrottle)
 *    - 오늘 본인 액션 수 ≥ recomputeActionThreshold(5) → 차단 (이미 카드 충분히 봤음)
 *    가입/복귀 3종은 자동 트리거라 어뷰즈 통로 X — 가드 미적용.
 *
 *  ── 가입은 ColdStart, 복귀는 정상 매칭 (사용자 합의) ──────────────────
 *    가입 : 본인 프로필 막 들어와 자격 풀 모집 늦을 가능성 → 인기/최근 풀로 카드 보장
 *    복귀 : 본인 프로필 이미 존재, 자격 풀 정상 → 정상 매칭. process 내부 풀 0명 fallback 으로 안전망.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchFeedRecomputeListener {

    private final UserManagement userMgmt;
    private final ColdStartFeed coldStartFeed;
    private final MyFeedRecomputer recomputer;
    private final MatchingConfigLoader configLoader;
    private final FeedRecomputeThrottle throttle;
    private final MatchActionRepository actionRepo;
    private final SystemErrorLogService systemErrorLogService;

    /* ─── (1) 가입 → ColdStart ─── */

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserCreated(UserCreatedEvent event) {
        try {
            UserContext me = userMgmt.loadContext(event.userId());
            if (me == null) {
                log.warn("[MatchFeedRecompute] 가입 — UserContext null, userId={}", event.userId());
                return;
            }
            coldStartFeed.build(me, configLoader.load());
        } catch (Exception e) {
            log.warn("[MatchFeedRecompute] 가입 ColdStart 실패 — userId={}, err={}",
                    event.userId(), e.getMessage(), e);
            systemErrorLogService.logStoreEvent(ErrorSeverity.WARN, "MatchFeedRecomputeListener.onUserCreated", event.userId(), e);
        }
    }

    /* ─── (2) 프로필 변경 → 어뷰즈 가드 적용 후 정상 매칭 ─── */

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProfileUpdated(ProfileUpdatedEvent event) {
        if (!event.kind().triggersRecompute()) return;
        if (!throttle.tryAcquire(event.userId())) {
            log.debug("[MatchFeedRecompute] 프로필 — throttle, userId={}", event.userId());
            return;
        }

        MatchingConfig cfg = configLoader.load();

        long todayActions = actionRepo.countTodayByActor(event.userId(), LocalDate.now());
        if (todayActions >= cfg.getRecomputeActionThreshold()) {
            log.debug("[MatchFeedRecompute] 프로필 — 액션 임계({}) 초과 차단. userId={}, actions={}",
                    cfg.getRecomputeActionThreshold(), event.userId(), todayActions);
            return;
        }

        try {
            recomputer.recompute(event.userId());
            log.debug("[MatchFeedRecompute] 프로필 재계산 완료 — userId={}, kind={}",
                    event.userId(), event.kind());
        } catch (Exception e) {
            log.warn("[MatchFeedRecompute] 프로필 재계산 실패 — userId={}, kind={}, err={}",
                    event.userId(), event.kind(), e.getMessage(), e);
            systemErrorLogService.logStoreEvent(ErrorSeverity.WARN, "MatchFeedRecomputeListener.onProfileUpdated", event.userId(), e);
        }
    }

    /* ─── (3)(4)(5) 복귀 3종 → 정상 매칭 (가드 없음, 자동 트리거) ─── */

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserReactivated(UserReactivatedEvent event) {
        try {
            recomputer.recompute(event.userId());
            log.info("[MatchFeedRecompute] 복귀 재계산 완료 — userId={}, reason={}",
                    event.userId(), event.reason());
        } catch (Exception e) {
            log.warn("[MatchFeedRecompute] 복귀 재계산 실패 — userId={}, reason={}, err={}",
                    event.userId(), event.reason(), e.getMessage(), e);
            systemErrorLogService.logStoreEvent(ErrorSeverity.WARN, "MatchFeedRecomputeListener.onUserReactivated", event.userId(), e);
        }
    }
}
