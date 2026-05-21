package com.nokcha.efbe.domain.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 내가 차단한 유저 한 명 — 차단 목록 화면용.
@Getter
@Builder
public class BlockedUserRspDto {

    private Long id;                // block id
    private Long blockedUserId;
    private String nickname;
    private Integer age;
    private String area;
    private String reasonCategory;
    private String detail;
    private LocalDateTime createTime;

    public static BlockedUserRspDto of(Block b, String area) {
        User blocked = b.getBlocked();
        return BlockedUserRspDto.builder()
                .id(b.getId())
                .blockedUserId(blocked.getId())
                .nickname(blocked.getNickname())
                .age(blocked.getAge())
                .area(area)
                .reasonCategory(b.getReasonCategory().name())
                .detail(b.getDetail())
                .createTime(b.getCreateTime())
                .build();
    }
}
