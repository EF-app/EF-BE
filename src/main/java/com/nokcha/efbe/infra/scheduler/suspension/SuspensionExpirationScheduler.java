package com.nokcha.efbe.infra.scheduler.suspension;

import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.repository.UserSuspensionRepository;
import com.nokcha.efbe.domain.suspension.service.SuspensionService;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TEMPORARY 자동 만료 배치 — 매일 00:00 (Asia/Seoul).
 * ends_at < now AND is_lifted=false 인 TEMPORARY 활성 row 들 조회
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuspensionExpirationScheduler {

    private final UserSuspensionRepository userSuspensionRepository;
    private final UserRepository userRepository;
    private final SuspensionService suspensionService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void expireSuspensions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSuspension> expired = userSuspensionRepository.findJustExpiredSuspensions(now);
        if (expired.isEmpty()) {
            log.info("[SuspensionExpiration] no expired rows — skip");
            return;
        }

        // 만료 row 일괄 자동 해제 마킹
        Set<Long> affectedUserIds = new HashSet<>();
        for (UserSuspension row : expired) {
            row.liftAutomatically();
            affectedUserIds.add(row.getUser().getId());
        }

        // users.status 변경
        int statusChanged = 0;
        for (Long userId : affectedUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;
            var before = user.getStatus();
            suspensionService.evaluateAndUpdateStatus(user);
            if (user.getStatus() != before) {
                statusChanged++;
                log.info("[SuspensionExpiration] userId={} : {} → {}", userId, before, user.getStatus());
            }
        }

        log.info("[SuspensionExpiration] expired rows={}, affected users={}, status changed={}",
                expired.size(), affectedUserIds.size(), statusChanged);
    }
}
