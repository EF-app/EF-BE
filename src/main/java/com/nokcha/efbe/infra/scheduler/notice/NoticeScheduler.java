package com.nokcha.efbe.infra.scheduler.notice;

// [2026-05-16] domain/admin/notice/scheduler 에 잠시 옮겼다가 다시 infra/scheduler 로 복귀.
// 모든 스케줄러는 infra/scheduler 하위에 모은다는 방침에 따라 원위치 정착.
import com.nokcha.efbe.domain.admin.notice.service.AdminNoticeService;
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

//    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(name = "NoticeScheduler.publishDue", lockAtMostFor = "PT55S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishDueScheduledNotices() {
        adminNoticeService.publishDueScheduledNotices();
    }
}
