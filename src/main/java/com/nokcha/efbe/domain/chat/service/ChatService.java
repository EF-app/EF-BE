package com.nokcha.efbe.domain.chat.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.chat.dto.request.ChatMessageReqDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatMessageRspDto;
import com.nokcha.efbe.domain.chat.dto.response.ChatRoomRspDto;
import com.nokcha.efbe.domain.chat.entity.ChatMessage;
import com.nokcha.efbe.domain.chat.entity.ChatRoom;
import com.nokcha.efbe.domain.chat.repository.ChatMessageRepository;
import com.nokcha.efbe.domain.chat.repository.ChatRoomRepository;
import com.nokcha.efbe.domain.payment.service.DailyUsageService;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    // 무료 답장 한도 (5회/일 - 기획 기본값)
    private static final String ACTION_POST_REPLY = "POST_REPLY";
    private static final int FREE_POST_REPLY_LIMIT = 5;
    private static final String ANONYMOUS_DISPLAY_NAME = "익명";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostItRepository postItRepository;
    private final UserRepository userRepository;
    private final DailyUsageService dailyUsageService;

    // 첫 답장 - 채팅방 미존재 시 생성 + 첫 메시지 저장. 존재하면 메시지만 추가
    // 무료 한도: 답장 5회/일 (POST_REPLY) — partner 기준. user_daily_usage 카운터 기반.
    // 동일 방에 추가 메시지 보내는 건 한도 미차감 (consume 은 첫 답장 분기 안에서만).
    @Transactional
    public ChatMessageRspDto replyToPost(Long postId, Long partnerId, PostReplyReqDto req) {
        PostIt post = postItRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST));
        if (Boolean.TRUE.equals(post.getIsDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_POST);
        }
        if (post.isExpired()) throw new BusinessException(ErrorCode.POST_EXPIRED);
        if (post.getUser() == null) throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        if (post.getUser().getId().equals(partnerId)) {
            throw new BusinessException(ErrorCode.SELF_ACTION_FORBIDDEN);
        }

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        // 첫 답장 시 결정한 익명 정책은 그 방에서 영원히 유지 (이후 토글 불가)
        boolean partnerAnonymous = Boolean.TRUE.equals(req.getIsAnonymous());
        ChatRoom room = chatRoomRepository.findByPostIdAndPartnerId(post.getId(), partnerId)
                .orElseGet(() -> {
                    // 새 방 생성 = 새 답장 → 한도 차감. 기존 방에 추가 메시지 보내는 건 미차감.
                    dailyUsageService.consume(partnerId, ACTION_POST_REPLY, FREE_POST_REPLY_LIMIT);
                    // 닉네임 스냅샷 - 방 생성 시점의 표시 이름 고정. 익명이면 partnerDisplayName="익명".
                    ChatRoom created = chatRoomRepository.save(ChatRoom.builder()
                            .uuid(UUID.randomUUID().toString())
                            .post(post)
                            .postOwner(post.getUser())
                            .partner(partner)
                            .ownerDisplayName(post.getUser().getNickname())
                            .partnerDisplayName(partnerAnonymous ? ANONYMOUS_DISPLAY_NAME : partner.getNickname())
                            .isPartnerAnonymous(partnerAnonymous)
                            .build());
                    post.increaseReplyCount();
                    return created;
                });
        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .uuid(UUID.randomUUID().toString())
                .room(room).sender(partner).content(req.getContent()).build());
        return ChatMessageRspDto.from(saved);
    }

    // 내 채팅방 목록
    @Transactional(readOnly = true)
    public Page<ChatRoomRspDto> getMyRooms(Long userId, int page, int size) {
        return chatRoomRepository.findMyRooms(userId, PageRequest.of(page, size))
                .map(ChatRoomRspDto::from);
    }

    // 채팅방 메시지 목록
    @Transactional(readOnly = true)
    public Page<ChatMessageRspDto> getMessages(Long roomId, Long viewerId, int page, int size) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHAT_ROOM));
        ensureParticipant(room, viewerId);
        return chatMessageRepository.findByRoomIdOrderByCreateTimeAsc(roomId, PageRequest.of(page, size))
                .map(ChatMessageRspDto::from);
    }

    // 메시지 전송 (기존 방)
    @Transactional
    public ChatMessageRspDto sendMessage(Long roomId, Long senderId, ChatMessageReqDto req) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHAT_ROOM));
        ensureParticipant(room, senderId);
        if (!Boolean.TRUE.equals(room.getIsActive()) || Boolean.TRUE.equals(room.getIsClosed())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .uuid(UUID.randomUUID().toString())
                .room(room).sender(sender).content(req.getContent()).build());
        return ChatMessageRspDto.from(saved);
    }

    // 메시지 취소 (Soft) - 발신자 본인, read_at 없음 전제
    @Transactional
    public void cancelMessage(Long messageId, Long requesterId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHAT_MESSAGE));
        if (msg.getSender() == null || !msg.getSender().getId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (msg.getReadAt() != null) {
            throw new BusinessException(ErrorCode.MESSAGE_ALREADY_READ);
        }
        msg.cancel();
    }

    // 참여자 검증 (owner or partner)
    private void ensureParticipant(ChatRoom room, Long viewerId) {
        Long ownerId = room.getPostOwner() == null ? null : room.getPostOwner().getId();
        Long partnerId = room.getPartner() == null ? null : room.getPartner().getId();
        if (viewerId == null || (!viewerId.equals(ownerId) && !viewerId.equals(partnerId))) {
            throw new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT);
        }
    }
}
