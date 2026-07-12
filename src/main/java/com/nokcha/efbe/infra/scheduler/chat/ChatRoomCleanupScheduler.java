package com.nokcha.efbe.infra.scheduler.chat;

import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import com.nokcha.efbe.domain.chat.repository.ChatParticipantRepository;
import com.nokcha.efbe.domain.chat.repository.ChatReportEvidenceRepository;
import com.nokcha.efbe.domain.chat.repository.ChatRoomRepository;
import com.nokcha.efbe.infra.scheduler.SchedulerGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * is_delete=true 상태로 30일 지난 채팅방을 DB에서 물리 삭제한다.
 * update_time은 채팅방이 삭제 상태로 전환된 시점을 기준으로 사용한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomCleanupScheduler {

    private static final int RETENTION_DAYS = 30;
    private static final int BATCH_SIZE = 500;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatReportEvidenceRepository chatReportEvidenceRepository;
    private final SchedulerGuard schedulerGuard;

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    @SchedulerLock(name = "ChatRoomCleanupScheduler.deleteExpiredDeletedRooms", lockAtMostFor = "PT10M", lockAtLeastFor = "PT30S")
    @Transactional
    public void deleteExpiredDeletedRooms() {
        schedulerGuard.runGuarded("ChatRoomCleanupScheduler.deleteExpiredDeletedRooms", () -> {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
            List<ChatRoom> rooms = chatRoomRepository.findDeletedRoomsForPhysicalDelete(cutoff, PageRequest.of(0, BATCH_SIZE));
            if (rooms.isEmpty()) {
                return;
            }

            List<Long> roomIds = rooms.stream()
                    .map(ChatRoom::getId)
                    .toList();

            int evidenceDeleted = chatReportEvidenceRepository.deleteByChatRoomIds(roomIds);
            int participantDeleted = chatParticipantRepository.deleteByChatRoomIds(roomIds);
            chatRoomRepository.deleteAllInBatch(rooms);

            log.info("[ChatRoomCleanup] deleted rooms={}, participants={}, reportEvidences={}, cutoff={}",
                    rooms.size(), participantDeleted, evidenceDeleted, cutoff);
        });
    }
}
