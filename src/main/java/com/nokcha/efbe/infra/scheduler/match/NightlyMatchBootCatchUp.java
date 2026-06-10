package com.nokcha.efbe.infra.scheduler.match;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 부팅 catch-up — 04:00 이후 ~ 자정 사이에 부팅하면 그날 04:00 정상 배치를 못 돌렸을 가능성. - 서버 부팅 시 1회 실행
 *  {@link NightlyMatchBatch#recoverFailedViewers()} 가 "오늘 daily_feed row 없는 viewer" 만
 *  처리하는 idempotent 메서드라 그대로 재사용 — 정상 부팅(03:59 이전)이면 효과 없음.
 *
 *  ── 시각별 동작 ────────────────────────────────────────────
 *    03:59 이전 부팅 : 트리거 안 함. 그날 04:00 cron 이 정상 실행
 *    04:00 ~ 자정    : recoverFailedViewers 호출 → 누락 viewer 자동 복구
 *
 *  ── 안전망 ────────────────────────────────────────────────
 *    - {@code @SchedulerLock}: 메서드에 이미 걸려있어 멀티 인스턴스 부팅 시 1개만 실행
 *    - {@code @Profile("!test")}: 테스트 컨텍스트 무영향 (ShedLockConfig 비활성)
 *    - try/catch: 실패해도 부팅 막지 않음. lazy ColdStartFeed 가 viewer 별 자력 복구
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class NightlyMatchBootCatchUp {

    private static final LocalTime BATCH_RUN_AT = LocalTime.of(4, 0);
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final NightlyMatchBatch nightlyMatchBatch;

    @EventListener(ApplicationReadyEvent.class)
    public void catchUpIfMissed() {
        LocalTime now = LocalTime.now(KST);
        if (now.isBefore(BATCH_RUN_AT)) {
            log.info("[BootCatchUp] 04:00 이전 부팅 ({}) — catch-up skip. 정상 cron 이 처리.", now);
            return;
        }

        log.info("[BootCatchUp] 04:00 이후 부팅 ({}) — recoverFailedViewers 호출", now);
        try {
            nightlyMatchBatch.recoverFailedViewers();
        } catch (Exception e) {
            log.warn("[BootCatchUp] recoverFailedViewers 실패 — viewer 별 lazy ColdStartFeed 로 자력 복구 의존. err={}",
                    e.getMessage(), e);
        }
    }
}
