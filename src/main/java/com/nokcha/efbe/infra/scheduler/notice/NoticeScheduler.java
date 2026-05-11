package com.nokcha.efbe.infra.scheduler.notice;

import com.nokcha.efbe.domain.notice.service.NoticeService;
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

    private final NoticeService noticeService;

//    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(name = "NoticeScheduler.publishDue", lockAtMostFor = "PT55S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishDueScheduledNotices() {
        noticeService.publishDueScheduledNotices();
    }
}
