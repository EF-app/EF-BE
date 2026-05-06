package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 답장 채팅방 응답 DTO
// 익명 방의 partner 는 partnerId 마스킹 (null) — 표시 이름은 partnerDisplayName 으로만 노출.
@Getter
@Builder
public class PostChatRoomRspDto {
    private Long id;
    private String uuid;
    private Long postId;
    private Long postOwnerId;
    private Long partnerId;
    private String ownerDisplayName;
    private String partnerDisplayName;
    private boolean partnerAnonymous;
    private boolean active;
    private boolean closed;
    private LocalDateTime createTime;

    public static PostChatRoomRspDto from(PostChatRoom r) {
        boolean partnerAnonymous = Boolean.TRUE.equals(r.getIsPartnerAnonymous());
        return PostChatRoomRspDto.builder()
                .id(r.getId())
                .uuid(r.getUuid())
                .postId(r.getPost() == null ? null : r.getPost().getId())
                .postOwnerId(r.getPostOwner() == null ? null : r.getPostOwner().getId())
                .partnerId(partnerAnonymous ? null : (r.getPartner() == null ? null : r.getPartner().getId()))
                .ownerDisplayName(r.getOwnerDisplayName())
                .partnerDisplayName(r.getPartnerDisplayName())
                .partnerAnonymous(partnerAnonymous)
                .active(Boolean.TRUE.equals(r.getIsActive()))
                .closed(Boolean.TRUE.equals(r.getIsClosed()))
                .createTime(r.getCreateTime())
                .build();
    }
}
