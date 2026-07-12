package com.nokcha.efbe.infra.scheduler.user;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import com.nokcha.efbe.domain.user.entity.WithdrawStatus;
import com.nokcha.efbe.domain.user.repository.UserWithdrawalRepository;
import com.nokcha.efbe.domain.user.service.WithdrawalDestructionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 탈퇴 완료 파기 배치 — 매일 01:30 KST.
 *  유저별 독립 트랜잭션(WithdrawalDestructionService.destroy) — 한 건 실패가 다른 건에 영향 없음.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserWithdrawalScheduler {

    private final UserWithdrawalRepository userWithdrawalRepository;
    private final WithdrawalDestructionService withdrawalDestructionService;
    private final SystemErrorLogService systemErrorLogService;

    @Scheduled(cron = "0 30 1 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "UserWithdrawalScheduler.completeExpiredWithdrawals",
            lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void completeExpiredWithdrawals() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Long> targetIds = userWithdrawalRepository
                    .findAllByStatusAndScheduledDestroyAtLessThanEqual(WithdrawStatus.REQUESTED, now)
                    .stream()
                    .map(w -> w.getId())
                    .toList();
            if (targetIds.isEmpty()) {
                return;
            }

            int done = 0;
            int failed = 0;
            for (Long withdrawalId : targetIds) {
                try {
                    withdrawalDestructionService.destroy(withdrawalId);   // 유저별 독립 트랜잭션
                    done++;
                } catch (Exception e) {
                    failed++;
                    log.warn("[UserWithdrawalScheduler] 파기 실패 — withdrawalId={}, err={}",
                            withdrawalId, e.getMessage(), e);
                    systemErrorLogService.logStoreBatch(ErrorSeverity.WARN,
                            "UserWithdrawalScheduler.completeExpiredWithdrawals", withdrawalId, e);
                }
            }
            log.info("[UserWithdrawalScheduler] 완료 — 대상 {}, 파기 {}, 실패 {}", targetIds.size(), done, failed);
        } catch (Exception e) {
            // 배치 전체 중단(대상 조회 실패 등)
            systemErrorLogService.logStoreBatch(ErrorSeverity.ERROR,
                    "UserWithdrawalScheduler.completeExpiredWithdrawals", null, e);
            throw e;
        }
    }
}
