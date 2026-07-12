package com.nokcha.efbe.infra.scheduler.user;

import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.user.service.WithdrawalDestructionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 2년 미접속 휴면 계정 파기 배치 — 매일 02:00 KST (탈퇴 파기 배치 01:30 이후).
 *  users.last_active_at 이 2년 이전인 ACTIVE 계정을 파기 처리.
 *
 *  ⚠️ 30일 전 통지 단계는 아직 미구현 — 현재는 2년 경과 즉시 파기. 통지 도입 시 이 배치 앞단에 통지/유예 단계 추가 예정.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDormantPurgeScheduler {

    private static final int DORMANT_YEARS = 2;
    // 회당 처리 상한 — 대량 백로그를 한 실행에 통째로 적재하지 않도록 제한(초과분은 다음 실행에서 소진).
    private static final int MAX_PER_RUN = 2000;

    private final UserRepository userRepository;
    private final WithdrawalDestructionService withdrawalDestructionService;
    private final SystemErrorLogService systemErrorLogService;

    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "UserDormantPurgeScheduler.purgeDormant",
            lockAtMostFor = "PT1H", lockAtLeastFor = "PT1M")
    public void purgeDormant() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusYears(DORMANT_YEARS);
            List<Long> targetIds = userRepository.findActiveDormantUserIds(
                    threshold, PageRequest.of(0, MAX_PER_RUN));
            if (targetIds.isEmpty()) {
                return;
            }

            int done = 0;
            int failed = 0;
            for (Long userId : targetIds) {
                try {
                    withdrawalDestructionService.destroyDormant(userId); // 유저별 독립 트랜잭션
                    done++;
                } catch (Exception e) {
                    failed++;
                    log.warn("[UserDormantPurgeScheduler] 휴면 파기 실패 — userId={}, err={}",
                            userId, e.getMessage(), e);
                    systemErrorLogService.logStoreBatch(ErrorSeverity.WARN,
                            "UserDormantPurgeScheduler.purgeDormant", userId, e);
                }
            }
            log.info("[UserDormantPurgeScheduler] 완료 — 대상 {}, 파기 {}, 실패 {}", targetIds.size(), done, failed);
        } catch (Exception e) {
            // 배치 전체 중단(대상 조회 실패 등)
            systemErrorLogService.logStoreBatch(ErrorSeverity.ERROR,
                    "UserDormantPurgeScheduler.purgeDormant", null, e);
            throw e;
        }
    }
}
