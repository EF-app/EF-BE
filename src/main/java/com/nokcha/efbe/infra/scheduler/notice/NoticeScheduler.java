package com.nokcha.efbe.infra.scheduler.notice;

import com.nokcha.efbe.domain.admin.notice.service.AdminNoticeService;
import com.nokcha.efbe.infra.scheduler.SchedulerGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeScheduler {
    private final AdminNoticeService adminNoticeService;
    private final SchedulerGuard schedulerGuard;

    @Scheduled(cron = "0 */10 * * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "NoticeScheduler.publishDue", lockAtMostFor = "PT55S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishDueScheduledNotices() {
        schedulerGuard.runGuarded("NoticeScheduler.publishDueScheduledNotices",
                adminNoticeService::publishDueScheduledNotices);
    }
}
