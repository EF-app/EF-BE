package com.nokcha.efbe.domain.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 유저 차단 응답 DTO
@Getter
@Builder
@Schema(description = "유저 차단 응답")
public class BlockRspDto {

    @Schema(description = "차단 레코드 PK", example = "1")
    private Long id;

    @Schema(description = "차단한 유저 PK", example = "1")
    private Long blockerId;

    @Schema(description = "차단당한 유저 PK", example = "102")
    private Long blockedId;

    @Schema(description = "차단 일시")
    private LocalDateTime createTime;

    public static BlockRspDto from(Block b) {
        return BlockRspDto.builder()
                .id(b.getId())
                .blockerId(b.getBlocker().getId())
                .blockedId(b.getBlocked().getId())
                .createTime(b.getCreateTime())
                .build();
    }
}
