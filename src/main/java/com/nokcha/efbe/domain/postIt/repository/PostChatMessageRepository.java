package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostChatMessageRepository extends JpaRepository<PostChatMessage, Long> {

    Page<PostChatMessage> findByRoomIdOrderByCreateTimeAsc(Long roomId, Pageable pageable);
}
