package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.common.util.DisplayNameUtil;
import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "밸런스 게임 신청 응답")
public class BalApplyRspDto {

    @Schema(description = "신청 PK", example = "1")
    private Long id;

    @Schema(description = "신청한 유저 ID", example = "10")
    private Long userId;

    @Schema(description = "신청한 유저 닉네임 (탈퇴 시 null)", example = "용감한 다람쥐")
    private String userNickname;

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 A 표시용 이모지", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "신청 사유/배경 설명")
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "신청 상태", example = "PENDING")
    private BalApplyStatus status;

    @Schema(description = "관리자 메모")
    private String adminMemo;

    @Schema(description = "신청 등록 시각")
    private LocalDateTime createTime;

    public static BalApplyRspDto from(BalApply a) {
        return BalApplyRspDto.builder()
                .id(a.getId())
                .userId(a.getUser() == null ? null : a.getUser().getId())
                .userNickname(a.getUser() == null ? null : DisplayNameUtil.orWithdrawn(a.getUser().getNickname()))
                .optionA(a.getOptionA())
                .optionB(a.getOptionB())
                .optionAEmoji(a.getOptionAEmoji())
                .optionBEmoji(a.getOptionBEmoji())
                .description(a.getDescription())
                .categoryCode(a.getCategoryCode())
                .status(a.getStatus())
                .adminMemo(a.getAdminMemo())
                .createTime(a.getCreateTime())
                .build();
    }
}
