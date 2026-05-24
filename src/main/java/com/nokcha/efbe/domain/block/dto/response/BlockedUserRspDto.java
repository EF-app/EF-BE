package com.nokcha.efbe.domain.block.dto.response;

import com.nokcha.efbe.domain.block.entity.Block;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 내가 차단한 유저 한 명 — 차단 목록 화면용.
@Getter
@Builder
@Schema(description = "내가 차단한 유저 (차단 목록 항목)")
public class BlockedUserRspDto {

    @Schema(description = "차단 레코드 PK", example = "1")
    private Long id;                // block id

    @Schema(description = "차단당한 유저 PK", example = "102")
    private Long blockedUserId;

    @Schema(description = "차단당한 유저 닉네임", example = "밍밍")
    private String nickname;

    @Schema(description = "차단당한 유저 나이", example = "27")
    private Integer age;

    @Schema(description = "차단당한 유저 지역", example = "서울 강남구")
    private String area;

    @Schema(description = "차단 일시")
    private LocalDateTime createTime;

    public static BlockedUserRspDto of(Block b, String area) {
        User blocked = b.getBlocked();
        return BlockedUserRspDto.builder()
                .id(b.getId())
                .blockedUserId(blocked.getId())
                .nickname(blocked.getNickname())
                .age(blocked.getAge())
                .area(area)
                .createTime(b.getCreateTime())
                .build();
    }
}
