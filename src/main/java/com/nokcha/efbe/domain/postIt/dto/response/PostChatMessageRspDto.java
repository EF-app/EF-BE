package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 채팅 메시지 응답 DTO (취소 시 본문 치환)
// 익명 방의 partner 발신 메시지는 senderId 마스킹 + senderDisplayName="익명" 노출.
@Getter
@Builder
public class PostChatMessageRspDto {

    private static final String CANCELED_TEXT = "상대가 메시지를 취소했어요";

    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderDisplayName;
    private boolean senderAnonymous;
    private String content;
    private boolean canceled;
    private LocalDateTime readAt;
    private LocalDateTime createTime;

    public static PostChatMessageRspDto from(PostChatMessage m) {
        boolean canceled = Boolean.TRUE.equals(m.getIsDeleted());
        PostChatRoom room = m.getRoom();
        Long senderUserId = m.getSender() == null ? null : m.getSender().getId();
        Long partnerUserId = (room == null || room.getPartner() == null) ? null : room.getPartner().getId();
        boolean partnerAnonymous = room != null && Boolean.TRUE.equals(room.getIsPartnerAnonymous());
        boolean senderIsAnonymousPartner = partnerAnonymous && senderUserId != null && senderUserId.equals(partnerUserId);

        String displayName = null;
        if (room != null) {
            if (senderIsAnonymousPartner) {
                displayName = room.getPartnerDisplayName();
            } else if (senderUserId != null && room.getPostOwner() != null && senderUserId.equals(room.getPostOwner().getId())) {
                displayName = room.getOwnerDisplayName();
            } else if (senderUserId != null && partnerUserId != null && senderUserId.equals(partnerUserId)) {
                displayName = room.getPartnerDisplayName();
            }
        }

        return PostChatMessageRspDto.builder()
                .id(m.getId())
                .roomId(room == null ? null : room.getId())
                .senderId(senderIsAnonymousPartner ? null : senderUserId)
                .senderDisplayName(displayName)
                .senderAnonymous(senderIsAnonymousPartner)
                .content(canceled ? CANCELED_TEXT : m.getContent())
                .canceled(canceled)
                .readAt(m.getReadAt())
                .createTime(m.getCreateTime())
                .build();
    }
}
