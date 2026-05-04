package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 답장 채팅방 응답 DTO
@Getter
@Builder
public class PostChatRoomRspDto {
    private Long id;
    private String uuid;
    private Long postId;
    private Long postOwnerId;
    private Long partnerId;
    private boolean active;
    private boolean closed;
    private LocalDateTime createTime;

    public static PostChatRoomRspDto from(PostChatRoom r) {
        return PostChatRoomRspDto.builder()
                .id(r.getId())
                .uuid(r.getUuid())
                .postId(r.getPost() == null ? null : r.getPost().getId())
                .postOwnerId(r.getPostOwner() == null ? null : r.getPostOwner().getId())
                .partnerId(r.getPartner() == null ? null : r.getPartner().getId())
                .active(Boolean.TRUE.equals(r.getIsActive()))
                .closed(Boolean.TRUE.equals(r.getIsClosed()))
                .createTime(r.getCreateTime())
                .build();
    }
}
