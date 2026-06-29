package com.nokcha.efbe.infra.scheduler.suspension;

import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.entity.UserStatus;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import com.nokcha.efbe.domain.errorLog.entity.ErrorSeverity;
import com.nokcha.efbe.domain.errorLog.service.SystemErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//users.status ↔ user_suspension 정합성 검증 배치 — 매주 월요일 03:00 (Asia/Seoul).

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspensionIntegrityScheduler {

    private final UserRepository userRepository;
    private final SuspensionService suspensionService;
    private final SystemErrorLogService systemErrorLogService;

    @Scheduled(cron = "0 0 3 * * MON", zone = "Asia/Seoul")
    @Transactional
    public void verifyIntegrity() {
        try {
            List<User> users = userRepository.findAllNonWithdrawn();
            int fixed = 0;

            for (User user : users) {
                UserStatus before = user.getStatus();
                UserStatus expected = suspensionService.evaluateUserStatus(user.getId());
                if (before != expected) {
                    user.changeStatus(expected);
                    fixed++;
                    log.warn("[SuspensionIntegrity] 불일치 복구: userId={} {} → {}",
                            user.getId(), before, expected);
                }
            }

            log.info("[SuspensionIntegrity] scanned={}, fixed={}", users.size(), fixed);
        } catch (Exception e) {
            systemErrorLogService.logStoreBatch(ErrorSeverity.ERROR, "SuspensionIntegrityScheduler.verifyIntegrity", null, e);
            throw e;
        }
    }
}
