package com.nokcha.efbe.domain.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 유저 차단 응답 DTO
@Getter
@Builder
public class BlockRspDto {

    private Long id;
    private Long blockerId;
    private Long blockedId;
    private String reasonCategory;
    private String detail;
    private LocalDateTime createTime;

    public static BlockRspDto from(Block b) {
        return BlockRspDto.builder()
                .id(b.getId())
                .blockerId(b.getBlocker().getId())
                .blockedId(b.getBlocked().getId())
                .reasonCategory(b.getReasonCategory().name())
                .detail(b.getDetail())
                .createTime(b.getCreateTime())
                .build();
    }
}
