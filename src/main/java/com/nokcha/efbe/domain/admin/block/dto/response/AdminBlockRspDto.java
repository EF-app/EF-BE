package com.nokcha.efbe.domain.admin.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 어드민 차단 내역 한 줄 — 차단자/피차단자의 닉네임·UUID 를 조인해 보강(enrich).
@Getter
@Builder
@Schema(description = "어드민 차단 내역 응답")
public class AdminBlockRspDto {

    @Schema(description = "차단 레코드 PK", example = "9101")
    private Long id;

    @Schema(description = "차단한 유저 PK", example = "101")
    private Long blockerId;

    @Schema(description = "차단한 유저 닉네임", example = "달빛여우")
    private String blockerNickname;

    @Schema(description = "차단한 유저 UUID")
    private String blockerUuid;

    @Schema(description = "차단당한 유저 PK", example = "104")
    private Long blockedId;

    @Schema(description = "차단당한 유저 닉네임", example = "차가운바람")
    private String blockedNickname;

    @Schema(description = "차단당한 유저 UUID")
    private String blockedUuid;

    /** 역방향 차단(blocked→blocker)도 존재하면 true — 상호 차단 */
    @Schema(description = "상호 차단 여부 — 역방향 차단(blocked→blocker)도 존재하면 true", example = "false")
    private boolean mutual;

    @Schema(description = "차단 일시")
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
                .mutual(mutual)
                .createTime(b.getCreateTime())
                .build();
    }
}
