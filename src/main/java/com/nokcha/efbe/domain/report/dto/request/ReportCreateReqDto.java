package com.nokcha.efbe.domain.report.dto.request;

import com.nokcha.efbe.domain.report.entity.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReportCreateReqDto {

    @NotNull
    private ReportTargetType targetType;

    @NotNull
    @Positive
    private Long targetId;

    // 화면 사유 칩 다중 선택 — enum 코드 모음 (HATE / SEXUAL / SPAM / THREAT / IMPERSONATE / OTHER 등).
    // 전체 직렬화 후 255 자 컷이 들어가므로 클라이언트가 알아서 합리적 길이로 보낸다고 가정.
    private List<String> reasonCodes;

    // 화면의 "신고 사유 상세 입력" 자유 텍스트.
    @Size(max = 500)
    private String detail;
}
