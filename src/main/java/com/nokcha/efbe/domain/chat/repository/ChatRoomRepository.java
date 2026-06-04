package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 내 채팅방 목록
    @Query("select r from ChatRoom r " +
            "where r.isActive = true " +
            "and exists (select p.id from ChatParticipant p " +
            "            where p.chatRoom = r and p.user.id = :userId and p.leftAt is null) " +
            "order by coalesce(r.lastMessageAt, r.createTime) desc, r.id desc")
    List<ChatRoom> findMyRooms(@Param("userId") Long userId, Pageable pageable);

    // 내 채팅방 목록 - 커서 이후 페이지
    @Query("select r from ChatRoom r " +
            "where r.isActive = true " +
            "and exists (select p.id from ChatParticipant p " +
            "            where p.chatRoom = r and p.user.id = :userId and p.leftAt is null) " +
            "and (coalesce(r.lastMessageAt, r.createTime) < :cursorSortAt " +
            "or (coalesce(r.lastMessageAt, r.createTime) = :cursorSortAt and r.id < :cursorId)) " +
            "order by coalesce(r.lastMessageAt, r.createTime) desc, r.id desc")
    List<ChatRoom> findMyRoomsAfterCursor(@Param("userId") Long userId, @Param("cursorSortAt") LocalDateTime cursorSortAt, @Param("cursorId") Long cursorId, Pageable pageable);

    Optional<ChatRoom> findFirstByRoomTypeInAndPairUserAIdAndPairUserBIdAndIsActiveTrueOrderByCreateTimeDescIdDesc(
            List<ChatRoomType> roomTypes,
            Long pairUserAId,
            Long pairUserBId
    );
}
