package com.nokcha.efbe.domain.postIt.dto.response;

import com.nokcha.efbe.domain.postIt.entity.PostChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "포스트잇 채팅방 응답")
public class PostChatRoomRspDto {

    @Schema(description = "채팅방 ID", example = "21")
    private Long id;

    @Schema(description = "채팅방 UUID", example = "1f0a2e5b-6f38-4e91-b8c2-12c2a8c6d9e1")
    private String uuid;

    @Schema(description = "원본 포스트잇 ID", example = "77", nullable = true)
    private Long postId;

    @Schema(description = "포스트 작성자 userId", example = "10", nullable = true)
    private Long postOwnerId;

    @Schema(description = "채팅 상대 userId. 익명 파트너면 null", example = "14", nullable = true)
    private Long partnerId;

    @Schema(description = "포스트 작성자 표시 이름", example = "포스트 작성자")
    private String ownerDisplayName;

    @Schema(description = "채팅 상대 표시 이름", example = "익명의 대화상대")
    private String partnerDisplayName;

    @Schema(description = "채팅 상대가 익명인지 여부", example = "true")
    private boolean partnerAnonymous;

    @Schema(description = "활성 채팅방 여부", example = "true")
    private boolean active;

    @Schema(description = "종료된 채팅방 여부", example = "false")
    private boolean closed;

    @Schema(description = "채팅방 생성 시각", example = "2026-05-25T16:00:00")
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
