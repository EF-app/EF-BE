package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByUuid(String uuid);

    // 원글 + 답장자 단건 (답장 시 방 존재 여부 확인)
    Optional<ChatRoom> findByPostIdAndPartnerId(Long postId, Long partnerId);

    // 내 채팅방 목록 (owner 또는 partner)
    @Query("select r from ChatRoom r " +
            "where r.postOwner.id = :userId or r.partner.id = :userId " +
            "order by r.createTime desc")
    Page<ChatRoom> findMyRooms(@Param("userId") Long userId, Pageable pageable);

    // 특정 글의 모든 채팅방 (원글 Soft delete 시 비활성화)
    List<ChatRoom> findByPostId(Long postId);
}
