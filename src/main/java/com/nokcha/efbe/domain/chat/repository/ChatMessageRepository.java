package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByRoomIdOrderByCreateTimeAsc(Long roomId, Pageable pageable);
}
