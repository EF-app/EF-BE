package com.nokcha.efbe.infra.scheduler.notification;

import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import com.nokcha.efbe.domain.match.repository.MatchDailyFeedQueryRepository;
import com.nokcha.efbe.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyNotificationScheduler {

    private final MatchDailyFeedQueryRepository matchDailyFeedQueryRepository;
    private final BalGameRepository balGameRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "DailyNotificationScheduler.sendDailyMatch", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void sendDailyMatchNotification() {
        LocalDate today = LocalDate.now();
        List<Long> viewerIds = matchDailyFeedQueryRepository.findViewerIdsByFeedDate(today);
        if (viewerIds.isEmpty()) {
            log.info("[DailyNotificationScheduler] 오늘의 매칭 알림 대상 없음 — date={}", today);
            return;
        }

        notificationService.sendDailyMatchNotification(viewerIds);
        log.info("[DailyNotificationScheduler] 오늘의 매칭 알림 전송 요청 — date={}, users={}", today, viewerIds.size());
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "DailyNotificationScheduler.sendDailyBalGame", lockAtMostFor = "PT30M", lockAtLeastFor = "PT1M")
    public void sendDailyBalGameNotification() {
        if (!balGameRepository.existsByStatus(BalGameStatus.PUBLISHED)) {
            log.info("[DailyNotificationScheduler] 오늘의 밸런스 게임 알림 생략 — 공개된 밸런스 게임 없음");
            return;
        }

        notificationService.sendDailyBalGameNotificationToActiveUsers(null);
        log.info("[DailyNotificationScheduler] 오늘의 밸런스 게임 알림 전송 요청");
    }
}
