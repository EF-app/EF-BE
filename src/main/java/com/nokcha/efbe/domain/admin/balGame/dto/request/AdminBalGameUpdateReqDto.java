package com.nokcha.efbe.domain.admin.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 밸런스 게임 수정 요청 DTO (관리자용)
// null 정책: 키 미전송 또는 null = 변경 없음. scheduledAt/EndAt 클리어 의도는 clearXxx flag 로 명시.
@Getter
@NoArgsConstructor
@Schema(description = "밸런스 게임 수정 요청 (관리자용) — null 필드는 변경하지 않음. 일정 클리어는 clearXxx flag 사용.")
public class AdminBalGameUpdateReqDto {

    @Schema(description = "옵션 A 텍스트", example = "교통카드")
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰")
    private String optionB;

    @Schema(description = "옵션 A 부연설명")
    private String optionADesc;

    @Schema(description = "옵션 B 부연설명")
    private String optionBDesc;

    @Schema(description = "옵션 A 표시용 이모지", example = "💳")
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지", example = "🎧")
    private String optionBEmoji;

    @Schema(description = "게임 전체 배경 설명")
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    private BalCategoryCode categoryCode;

    @Schema(description = "게시 상태", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "예약 게시 시각")
    private LocalDateTime scheduledAt;

    @Schema(description = "예약 종료 시각")
    private LocalDateTime scheduledEndAt;

    @Schema(description = "true 면 scheduledAt 을 NULL 로 명시적 클리어. scheduledAt 값과 동시 사용 불가.", example = "false")
    private Boolean clearScheduledAt;

    @Schema(description = "true 면 scheduledEndAt 을 NULL 로 명시적 클리어. scheduledEndAt 값과 동시 사용 불가.", example = "false")
    private Boolean clearScheduledEndAt;

    // PUBLISHED 상태에서 내용 변경 시도 차단용 - 내용필드에서 채워졌는지
    public boolean hasContentField() {
        return optionA != null || optionB != null
                || optionADesc != null || optionBDesc != null
                || optionAEmoji != null || optionBEmoji != null
                || description != null || categoryCode != null;
    }
}
