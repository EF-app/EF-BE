package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

// 포스트잇 채팅 메시지 레포지토리
public interface PostChatMessageRepository extends JpaRepository<PostChatMessage, Long> {

    Page<PostChatMessage> findByRoomIdOrderByCreateTimeAsc(Long roomId, Pageable pageable);
}
