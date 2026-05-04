package com.nokcha.efbe.domain.postIt.service;

import com.nokcha.efbe.common.exception.BusinessException;
import com.nokcha.efbe.common.exception.ErrorCode;
import com.nokcha.efbe.domain.payment.service.DailyUsageService;
import com.nokcha.efbe.domain.postIt.dto.request.PostChatMessageReqDto;
import com.nokcha.efbe.domain.postIt.dto.request.PostReplyReqDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatMessageRspDto;
import com.nokcha.efbe.domain.postIt.dto.response.PostChatRoomRspDto;
import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import com.nokcha.efbe.domain.postIt.entity.PostIt;
import com.nokcha.efbe.domain.postIt.repository.PostChatMessageRepository;
import com.nokcha.efbe.domain.postIt.repository.PostChatRoomRepository;
import com.nokcha.efbe.domain.postIt.repository.PostItRepository;
import com.nokcha.efbe.domain.user.entity.User;
import com.nokcha.efbe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 포스트잇 답장 채팅 서비스 (첫 답장 시 방 생성, 메시지 송수신/취소)
@Service
@RequiredArgsConstructor
public class PostChatService {

    // 무료 답장 한도 (5회/일 - 기획 기본값)
    private static final String ACTION_POST_REPLY = "POST_REPLY";
    private static final int FREE_POST_REPLY_LIMIT = 5;

    private final PostChatRoomRepository postChatRoomRepository;
    private final PostChatMessageRepository postChatMessageRepository;
    private final PostItRepository postItRepository;
    private final UserRepository userRepository;
    private final DailyUsageService dailyUsageService;

    // 첫 답장 - 채팅방 미존재 시 생성 + 첫 메시지 저장. 존재하면 메시지만 추가
    // 무료 한도: POST_REPLY 5회/일
    @Transactional
    public PostChatMessageRspDto replyToPost(Long postId, Long partnerId, PostReplyReqDto req) {
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

        dailyUsageService.consume(partnerId, ACTION_POST_REPLY, FREE_POST_REPLY_LIMIT);

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        PostChatRoom room = postChatRoomRepository.findByPostIdAndPartnerId(postId, partnerId)
                .orElseGet(() -> {
                    // v1.6 닉네임 스냅샷 - 방 생성 시점의 표시 이름 고정
                    PostChatRoom created = postChatRoomRepository.save(PostChatRoom.builder()
                            .uuid(UUID.randomUUID().toString())
                            .post(post)
                            .postOwner(post.getUser())
                            .partner(partner)
                            .ownerDisplayName(post.getUser().getNickname())
                            .partnerDisplayName(partner.getNickname())
                            .build());
                    post.increaseReplyCount();
                    return created;
                });
        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }

        PostChatMessage saved = postChatMessageRepository.save(PostChatMessage.builder()
                .uuid(UUID.randomUUID().toString())
                .room(room).sender(partner).content(req.getContent()).build());
        return PostChatMessageRspDto.from(saved);
    }

    // 내 채팅방 목록
    @Transactional(readOnly = true)
    public Page<PostChatRoomRspDto> getMyRooms(Long userId, int page, int size) {
        return postChatRoomRepository.findMyRooms(userId, PageRequest.of(page, size))
                .map(PostChatRoomRspDto::from);
    }

    // 채팅방 메시지 목록
    @Transactional(readOnly = true)
    public Page<PostChatMessageRspDto> getMessages(Long roomId, Long viewerId, int page, int size) {
        PostChatRoom room = postChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST_CHAT_ROOM));
        ensureParticipant(room, viewerId);
        return postChatMessageRepository.findByRoomIdOrderByCreateTimeAsc(roomId, PageRequest.of(page, size))
                .map(PostChatMessageRspDto::from);
    }

    // 메시지 전송 (기존 방)
    @Transactional
    public PostChatMessageRspDto sendMessage(Long roomId, Long senderId, PostChatMessageReqDto req) {
        PostChatRoom room = postChatRoomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_POST_CHAT_ROOM));
        ensureParticipant(room, senderId);
        if (!Boolean.TRUE.equals(room.getIsActive()) || Boolean.TRUE.equals(room.getIsClosed())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_INACTIVE);
        }
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
        PostChatMessage saved = postChatMessageRepository.save(PostChatMessage.builder()
                .uuid(UUID.randomUUID().toString())
                .room(room).sender(sender).content(req.getContent()).build());
        return PostChatMessageRspDto.from(saved);
    }

    // 메시지 취소 (Soft) - 발신자 본인, read_at 없음 전제
    @Transactional
    public void cancelMessage(Long messageId, Long requesterId) {
        PostChatMessage msg = postChatMessageRepository.findById(messageId)
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
    private void ensureParticipant(PostChatRoom room, Long viewerId) {
        Long ownerId = room.getPostOwner() == null ? null : room.getPostOwner().getId();
        Long partnerId = room.getPartner() == null ? null : room.getPartner().getId();
        if (viewerId == null || (!viewerId.equals(ownerId) && !viewerId.equals(partnerId))) {
            throw new BusinessException(ErrorCode.CHAT_NOT_PARTICIPANT);
        }
    }
}
