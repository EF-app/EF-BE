package com.nokcha.efbe.domain.postIt.entity;

import com.nokcha.efbe.common.entity.BaseEntity;
import com.nokcha.efbe.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 포스트잇 답장 1:1 채팅방 엔티티 (post_chat_room)
// [v1.6] 방 생성 당시 닉네임 스냅샷 (owner/partner_display_name)
@Getter
@Entity
@Table(name = "post_chat_room",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_chat_uuid", columnNames = "uuid"),
                @UniqueConstraint(name = "uk_post_partner", columnNames = {"post_id", "partner_id"})
        },
        indexes = {
                @Index(name = "idx_chat_owner", columnList = "post_owner_id, create_time DESC"),
                @Index(name = "idx_chat_partner", columnList = "partner_id, create_time DESC")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, length = 36)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_post"))
    private PostIt post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_owner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_owner"))
    private User postOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_partner"))
    private User partner;

    // [v1.6] 방 생성 당시 닉네임 스냅샷 (이후 변경돼도 표시 이름은 불변)
    @Column(name = "owner_display_name", nullable = false, length = 30)
    private String ownerDisplayName;

    @Column(name = "partner_display_name", nullable = false, length = 30)
    private String partnerDisplayName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = Boolean.FALSE;

    @Builder
    private PostChatRoom(String uuid, PostIt post, User postOwner, User partner,
                         String ownerDisplayName, String partnerDisplayName) {
        this.uuid = uuid;
        this.post = post;
        this.postOwner = postOwner;
        this.partner = partner;
        this.ownerDisplayName = ownerDisplayName;
        this.partnerDisplayName = partnerDisplayName;
        this.isActive = Boolean.TRUE;
        this.isClosed = Boolean.FALSE;
    }

    // 원글 Soft delete 시 비활성화 (진입 시 "원문이 삭제된 포스트잇입니다" 표시)
    public void deactivate() {
        this.isActive = Boolean.FALSE;
    }

    // 한쪽이 나간 경우 종료
    public void close() {
        this.isClosed = Boolean.TRUE;
    }
}
