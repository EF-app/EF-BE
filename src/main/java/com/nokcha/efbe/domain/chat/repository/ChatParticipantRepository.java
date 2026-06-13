package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatParticipant;
import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    Optional<ChatParticipant> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);

    List<ChatParticipant> findByChatRoom_Id(Long chatRoomId);

    @Query("""
            select p
            from ChatParticipant p
            join fetch p.user
            where p.chatRoom.id = :chatRoomId
            """)
    List<ChatParticipant> findWithUserByChatRoom_Id(@Param("chatRoomId") Long chatRoomId);

    @Query("""
            select p
            from ChatParticipant p
            join fetch p.user
            where p.chatRoom.id in :chatRoomIds
            """)
    List<ChatParticipant> findWithUserByChatRoom_IdIn(@Param("chatRoomIds") Collection<Long> chatRoomIds);

    boolean existsByChatRoom_RoomTypeAndChatRoom_Post_IdAndUser_IdAndLeftAtIsNull(
            ChatRoomType roomType,
            Long postId,
            Long userId
    );

    boolean existsByChatRoom_IdAndLeftAtIsNull(Long chatRoomId);
}
