package com.nokcha.efbe.domain.admin.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 차단 내역 한 줄 — 차단자/피차단자의 닉네임·UUID 를 조인해 보강(enrich).
@Getter
@Builder
public class AdminBlockRspDto {

    private Long id;
    private Long blockerId;
    private String blockerNickname;
    private String blockerUuid;
    private Long blockedId;
    private String blockedNickname;
    private String blockedUuid;
    private String reasonCategory;
    private String detail;
    /** 역방향 차단(blocked→blocker)도 존재하면 true — 상호 차단 */
    private boolean mutual;
    private LocalDateTime createTime;

    public static AdminBlockRspDto from(Block b, boolean mutual) {
        User blocker = b.getBlocker();
        User blocked = b.getBlocked();
        return AdminBlockRspDto.builder()
                .id(b.getId())
                .blockerId(blocker.getId())
                .blockerNickname(blocker.getNickname())
                .blockerUuid(blocker.getUuid())
                .blockedId(blocked.getId())
                .blockedNickname(blocked.getNickname())
                .blockedUuid(blocked.getUuid())
                .reasonCategory(b.getReasonCategory().name())
                .detail(b.getDetail())
                .mutual(mutual)
                .createTime(b.getCreateTime())
                .build();
    }
}
