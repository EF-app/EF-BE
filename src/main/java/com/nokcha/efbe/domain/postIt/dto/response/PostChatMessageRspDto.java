package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포스트잇 채팅 메시지 응답")
public class PostChatMessageRspDto {

    private static final String CANCELED_TEXT = "상대가 메시지를 취소했어요";

    @Schema(description = "메시지 ID", example = "301")
    private Long id;

    @Schema(description = "채팅방 ID", example = "21")
    private Long roomId;

    @Schema(description = "발신자 userId. 익명 파트너 메시지면 null", example = "14", nullable = true)
    private Long senderId;

    @Schema(description = "발신자 표시 이름", example = "익명의 대화상대")
    private String senderDisplayName;

    @Schema(description = "발신자가 익명 파트너인지 여부", example = "true")
    private boolean senderAnonymous;

    @Schema(description = "메시지 내용. 취소된 메시지는 치환 문구가 내려감.", example = "안녕하세요!")
    private String content;

    @Schema(description = "메시지 취소 여부", example = "false")
    private boolean canceled;

    @Schema(description = "읽음 시각. 아직 읽지 않았으면 null", example = "2026-05-25T16:10:00", nullable = true)
    private LocalDateTime readAt;

    @Schema(description = "메시지 생성 시각", example = "2026-05-25T16:05:00")
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
