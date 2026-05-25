package com.nokcha.efbe.domain.report.dto.request;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "신고 등록 요청 — 사유 칩 다중 선택 + 자유 텍스트 상세")
public class ReportCreateReqDto {

    @Schema(description = "신고 대상 유형", example = "POST_IT", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 PK", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Positive
    private Long targetId;

    // 화면 사유 칩 다중 선택 — enum 코드 모음 (HATE / SEXUAL / SPAM / THREAT / IMPERSONATE / OTHER 등).
    // 전체 직렬화 후 255 자 컷이 들어가므로 클라이언트가 알아서 합리적 길이로 보낸다고 가정.
    @Schema(description = "사유 칩 다중 선택 — HATE / SEXUAL / SPAM / THREAT / IMPERSONATE / OTHER 등",
            example = "[\"HATE\", \"SPAM\"]", nullable = true)
    private List<String> reasonCodes;

    // 화면의 "신고 사유 상세 입력" 자유 텍스트.
    @Schema(description = "신고 사유 상세 (자유 텍스트, 최대 500자)",
            example = "반복적으로 욕설을 보냅니다.", nullable = true, maxLength = 500)
    @Size(max = 500)
    private String detail;
}
