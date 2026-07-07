package com.nokcha.efbe.infra.scheduler.payment;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import com.nokcha.efbe.domain.payment.entity.UserPalette;
import com.nokcha.efbe.domain.payment.repository.UserPaletteRepository;
import com.nokcha.efbe.domain.payment.service.PaletteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구독 만료 배치 — 매일 00:10 (Asia/Seoul). 최근 만료된 팔레트 구독에 EXPIRE 이력을 남긴다.
 *
 * 상태(premium_until)는 조회 시 자동 무료 판정되므로 기능상 필수는 아니고, 감사·알림 훅용 이력이다.
 * markExpired 가 멱등(미만료/중복 스킵)이라 재실행·catch-up 안전.
 *
 * cron 진입 {@link #run()} 에만 @SchedulerLock — 멀티 인스턴스 중복 차단.
 * 본문 {@link #runExpiry(LocalDateTime)} 는 락 없이 분리 → 관리자 수동 트리거 재사용 가능.
 *
 * ※ 자동갱신(auto_renew) 실제 재결제는 정기결제(recurring billing) 인프라가 없어 미구현.
 *   현재는 auto_renew=true 여도 재결제가 없으면 그대로 만료 처리된다. 정기결제 도입 시 별도 배치 추가.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionExpirationScheduler {

    // 배치 누락(다운타임) 대비 catch-up 윈도우.
    private static final int CATCHUP_DAYS = 2;

    private final UserPaletteRepository userPaletteRepository;
    private final PaletteService paletteService;
    private final SystemErrorLogService systemErrorLogService;

    @Scheduled(cron = "0 10 0 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "SubscriptionExpirationScheduler.run", lockAtMostFor = "PT10M", lockAtLeastFor = "PT30S")
    public void run() {
        runExpiry(LocalDateTime.now());
    }

    /** 락 없는 본문 — 최근 만료 구독에 EXPIRE 기록. 대상별 독립 트랜잭션, 실패는 격리 후 루프 계속. */
    public void runExpiry(LocalDateTime now) {
        List<UserPalette> candidates =
                userPaletteRepository.findByPremiumUntilBetween(now.minusDays(CATCHUP_DAYS), now);
        if (candidates.isEmpty()) {
            log.info("[SubscriptionExpiration] no candidates — skip");
            return;
        }

        int done = 0;
        int failed = 0;
        for (UserPalette palette : candidates) {
            try {
                paletteService.markExpired(palette.getUserId()); // 독립 트랜잭션 + 멱등
                done++;
            } catch (Exception e) {
                failed++;
                systemErrorLogService.logStoreBatch(ErrorSeverity.ERROR,
                        "SubscriptionExpirationScheduler.runExpiry", palette.getUserId(), e);
            }
        }
        log.info("[SubscriptionExpiration] candidates={}, expired-logged={}, failed={}",
                candidates.size(), done, failed);
    }
}
