package com.nokcha.efbe.domain.admin.suspension.dto.request;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관리자 제재 부과 요청")
public class AdminSuspensionCreateReqDto {

    @NotNull
    @Schema(description = "제재 대상 유저 PK", example = "1042")
    private Long targetUserId;

    @NotNull
    @Schema(description = "제재 유형", example = "TEMPORARY")
    private SuspensionType type;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "제재 사유 (유저 통보)", example = "허위 정보 게시")
    private String reason;

    @Positive
    @Schema(description = "TEMPORARY 일 때 필수 — 정지 일수 (예: 7, 30)", example = "7")
    private Integer durationDays;

    @Schema(description = "제재 근거 신고 대상 타입. 임의 제재 시 null", example = "POST_IT")
    private ReportTargetType sourceTargetType;

    @Schema(description = "제재 근거 대상 PK. 임의 제재 시 null", example = "1234")
    private Long sourceTargetId;
}
