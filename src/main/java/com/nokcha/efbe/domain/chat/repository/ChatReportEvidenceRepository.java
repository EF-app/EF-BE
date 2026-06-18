package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatReportEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ChatReportEvidenceRepository extends JpaRepository<ChatReportEvidence, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatReportEvidence e where e.chatRoom.id in :chatRoomIds")
    int deleteByChatRoomIds(@Param("chatRoomIds") Collection<Long> chatRoomIds);
}
