package com.nokcha.efbe.domain.balGame.dto.response;

import com.nokcha.efbe.domain.balGame.entity.BalApply;
import com.nokcha.efbe.domain.balGame.entity.BalApplyStatus;
import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 밸런스 게임 신청 응답 DTO
@Getter
@Builder
public class BalApplyRspDto {
    private Long id;
    private Long userId;
    private String optionA;
    private String optionB;
    private String optionAEmoji;
    private String optionBEmoji;
    private String description;
    private BalCategoryCode categoryCode;
    private BalApplyStatus status;
    private String adminMemo;
    private LocalDateTime createTime;

    public static BalApplyRspDto from(BalApply a) {
        return BalApplyRspDto.builder()
                .id(a.getId())
                .userId(a.getUser() == null ? null : a.getUser().getId())
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
