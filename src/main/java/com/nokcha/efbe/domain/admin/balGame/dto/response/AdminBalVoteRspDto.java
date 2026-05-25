package com.nokcha.efbe.domain.admin.balGame.dto.response;

import com.nokcha.efbe.common.util.LocationUtil;
import com.nokcha.efbe.domain.admin.balGame.repository.projection.AdminBalVoteRow;
import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "어드민 밸런스 게임 개별 투표자")
public class AdminBalVoteRspDto {

    @Schema(description = "투표 PK", example = "100")
    private Long voteId;

    @Schema(description = "투표자 user id", example = "42")
    private Long userId;

    @Schema(description = "투표자 user uuid")
    private String userUuid;

    @Schema(description = "투표자 닉네임 (탈퇴 시 null)", example = "용감한 다람쥐")
    private String userNickname;

    @Schema(description = "투표자 나이", example = "27")
    private Integer userAge;

    @Schema(description = "투표자 지역 (country + city)", example = "서울특별시 강남구")
    private String userArea;

    @Schema(description = "투표 선택지", example = "A")
    private BalVoteChoice choice;

    @Schema(description = "첫 투표 시각")
    private LocalDateTime createTime;

    @Schema(description = "재투표 시 갱신 시각")
    private LocalDateTime updateTime;

    public static AdminBalVoteRspDto from(AdminBalVoteRow r) {
        return AdminBalVoteRspDto.builder()
                .voteId(r.id())
                .userId(r.userId())
                .userUuid(r.userUuid())
                .userNickname(r.userNickname())
                .userAge(r.userAge())
                .userArea(composeLocation(r.areaCountry(), r.areaCity()))
                .choice(r.choice())
                .createTime(r.createTime())
                .updateTime(r.updateTime())
                .build();
    }
}
