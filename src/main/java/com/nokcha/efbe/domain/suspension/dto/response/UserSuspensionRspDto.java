package com.nokcha.efbe.domain.suspension.dto.response;

import com.nokcha.efbe.domain.suspension.entity.UserSuspension;
import com.nokcha.efbe.domain.suspension.entity.SuspensionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// 유저 본인의 활성 제재 정보 — "제재중" 화면 표시용.
@Getter
@Builder
@Schema(description = "유저 본인의 활성 제재 정보 (없으면 isActive=false)")
public class UserSuspensionRspDto {

    @Schema(description = "활성 제재 존재 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "제재 유형", example = "TEMPORARY", nullable = true)
    private SuspensionType type;

    @Schema(description = "제재 사유", example = "허위 정보 게시", nullable = true)
    private String reason;

    @Schema(description = "제재 시작 시각", example = "2026-05-26T10:00:00", nullable = true)
    private LocalDateTime startsAt;

    @Schema(description = "제재 종료 시각. PERMANENT 면 null", example = "2026-06-02T00:00:00", nullable = true)
    private LocalDateTime endsAt;

    public static UserSuspensionRspDto inactive() {
        return UserSuspensionRspDto.builder().isActive(false).build();
    }

    public static UserSuspensionRspDto from(UserSuspension s) {
        return UserSuspensionRspDto.builder()
                .isActive(true)
                .type(s.getSuspensionType())
                .reason(s.getReason())
                .startsAt(s.getStartsAt())
                .endsAt(s.getEndsAt())
                .build();
    }
}
