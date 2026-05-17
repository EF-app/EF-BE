package com.nokcha.efbe.infra.scheduler.user;

import com.nokcha.efbe.domain.user.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// 만료된 revoked_token row 정리. 토큰이 만료되면 어차피 검증 단계에서 무효라 blacklist 에 둘 의미 없음.
// 매일 03:00 실행.
@Slf4j
@Component
@RequiredArgsConstructor
public class RevokedTokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void cleanupExpired() {
        int deleted = revokedTokenRepository.deleteAllExpired(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[RevokedTokenCleanup] 만료된 폐기 토큰 row 삭제: {}", deleted);
        }
    }
}
