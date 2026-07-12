package com.nokcha.efbe.domain.chat.repository;

import com.nokcha.efbe.domain.chat.entity.ChatParticipant;
import com.nokcha.efbe.domain.chat.entity.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ChatParticipant p where p.chatRoom.id in :chatRoomIds")
    int deleteByChatRoomIds(@Param("chatRoomIds") Collection<Long> chatRoomIds);

    // 탈퇴 파기용 — 대화방 유지 + 발신자 닉네임 스냅샷만 익명화("탈퇴한 회원")
    //  ⚠️ clearAutomatically 금지 — 같은 트랜잭션에서 이후 실행되는 user.anonymize()/withdrawal.complete()
    @Modifying
    @Query("update ChatParticipant p set p.displayName = :label where p.user.id = :userId")
    int anonymizeDisplayNameByUserId(@Param("userId") Long userId, @Param("label") String label);
}
