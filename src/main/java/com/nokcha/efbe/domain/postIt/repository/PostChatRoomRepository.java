package com.nokcha.efbe.domain.postIt.repository;

import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 포스트잇 답장 채팅방 레포지토리
public interface PostChatRoomRepository extends JpaRepository<PostChatRoom, Long> {

    Optional<PostChatRoom> findByUuid(String uuid);

    // 원글 + 답장자 단건 (답장 시 방 존재 여부 확인)
    Optional<PostChatRoom> findByPostIdAndPartnerId(Long postId, Long partnerId);

    // 내 채팅방 목록 (owner 또는 partner)
    @Query("select r from PostChatRoom r " +
            "where r.postOwner.id = :userId or r.partner.id = :userId " +
            "order by r.createTime desc")
    Page<PostChatRoom> findMyRooms(@Param("userId") Long userId, Pageable pageable);

    // 특정 글의 모든 채팅방 (원글 Soft delete 시 비활성화)
    List<PostChatRoom> findByPostId(Long postId);
}
