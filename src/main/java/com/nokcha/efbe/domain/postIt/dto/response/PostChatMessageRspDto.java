package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatMessage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 포스트잇 채팅 메시지 응답 DTO (취소 시 본문 치환)
@Getter
@Builder
public class PostChatMessageRspDto {

    private static final String CANCELED_TEXT = "상대가 메시지를 취소했어요";

    private Long id;
    private Long roomId;
    private Long senderId;
    private String content;
    private boolean canceled;
    private LocalDateTime readAt;
    private LocalDateTime createTime;

    public static PostChatMessageRspDto from(PostChatMessage m) {
        boolean canceled = Boolean.TRUE.equals(m.getIsDeleted());
        return PostChatMessageRspDto.builder()
                .id(m.getId())
                .roomId(m.getRoom() == null ? null : m.getRoom().getId())
                .senderId(m.getSender() == null ? null : m.getSender().getId())
                .content(canceled ? CANCELED_TEXT : m.getContent())
                .canceled(canceled)
                .readAt(m.getReadAt())
                .createTime(m.getCreateTime())
                .build();
    }
}
