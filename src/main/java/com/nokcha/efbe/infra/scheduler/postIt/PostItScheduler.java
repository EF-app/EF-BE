package com.nokcha.efbe.infra.scheduler.postIt;

import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 포스트잇 만료/상단고정 해제 스케줄러 (매 분)
@Slf4j
@Component
@RequiredArgsConstructor
public class PostItScheduler {

    private final PostItRepository postItRepository;

    // 만료된 포스트잇의 고정 해제 (만료 후에는 피드에서 자연스럽게 빠지므로 추가 상태 전이 불필요)
//    @Scheduled(cron = "0 * * * * *")
    @SchedulerLock(name = "PostItScheduler.expirePins", lockAtMostFor = "PT55S", lockAtLeastFor = "PT5S")
    @Transactional
    public void expirePins() {
        LocalDateTime now = LocalDateTime.now();
        List<PostIt> expired = postItRepository.findExpiredPins(now);
        if (expired.isEmpty()) return;
        expired.forEach(PostIt::clearPin);
        log.info("[PostItScheduler] cleared {} expired pins at {}", expired.size(), now);
    }
}
