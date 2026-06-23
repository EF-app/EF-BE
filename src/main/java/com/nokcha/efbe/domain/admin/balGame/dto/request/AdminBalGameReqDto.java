package com.nokcha.efbe.domain.admin.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalCategoryCode;
import com.nokcha.efbe.domain.balGame.entity.BalGameStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 밸런스 게임 등록/수정 요청")
public class AdminBalGameReqDto {

    @Schema(description = "옵션 A 텍스트", example = "교통카드", maxLength = 255)
    @NotBlank
    @Size(max = 255)
    private String optionA;

    @Schema(description = "옵션 B 텍스트", example = "이어폰", maxLength = 255)
    @NotBlank
    @Size(max = 255)
    private String optionB;

    @Schema(description = "옵션 A 부연설명 (선택)", maxLength = 500)
    @Size(max = 500)
    private String optionADesc;

    @Schema(description = "옵션 B 부연설명 (선택)", maxLength = 500)
    @Size(max = 500)
    private String optionBDesc;

    @Schema(description = "옵션 A 표시용 이모지 (선택)", example = "💳", maxLength = 8)
    @Size(max = 8)
    private String optionAEmoji;

    @Schema(description = "옵션 B 표시용 이모지 (선택)", example = "🎧", maxLength = 8)
    @Size(max = 8)
    private String optionBEmoji;

    @Schema(description = "게임 전체 배경 설명 (선택)", maxLength = 1000)
    @Size(max = 1000)
    private String description;

    @Schema(description = "카테고리", example = "DAILY")
    @NotNull
    private BalCategoryCode categoryCode;

    @Schema(description = "BAL-APPLY 기반 등록 시 신청 id로, 있으면 BalApply 가 PENDING → APPROVED 처리되고 신청자가 applicant 로 연결됨. (관리자 등록일 경우 생략)")
    private Long applyId;

    @Schema(description = "게시 상태 (생략 시 DRAFT)", example = "PUBLISHED")
    private BalGameStatus status;

    @Schema(description = "예약 게시 시각")
    private LocalDateTime scheduledAt;

    @Schema(description = "예약 종료 시각")
    private LocalDateTime scheduledEndAt;

    // 게시된 밸런스 게임 수정 차단 메소드
    public boolean hasContentField() {
        return optionA != null || optionB != null
                || optionADesc != null || optionBDesc != null
                || optionAEmoji != null || optionBEmoji != null
                || description != null || categoryCode != null;
    }
}
