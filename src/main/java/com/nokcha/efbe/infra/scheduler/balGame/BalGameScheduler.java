package com.nokcha.efbe.infra.scheduler.balGame;

import com.nokcha.efbe.domain.balGame.entity.BalGame;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import com.nokcha.efbe.domain.balGame.repository.BalGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 예약(SCHEDULED) 게시 자동 공개(PUBLISHED) 전환 스케줄러 (매 분 실행)
@Slf4j
@Component
@RequiredArgsConstructor
public class BalGameScheduler {

    private final BalGameRepository balGameRepository;

    // 매 분 0초에 scheduled_at 이 지난 SCHEDULED 게임을 PUBLISHED 로 전환
//    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "BalGameScheduler.publishDue", lockAtMostFor = "PT55S", lockAtLeastFor = "PT5S")
    @Transactional
    public void publishDueScheduledGames() {
        LocalDateTime now = LocalDateTime.now();
        List<BalGame> due = balGameRepository.findDueScheduled(BalGameStatus.SCHEDULED, now);
        if (due.isEmpty()) return;

        due.forEach(g -> g.changeStatus(BalGameStatus.PUBLISHED));
        log.info("[BalGameScheduler] published {} scheduled balance games at {}", due.size(), now);
    }
}
