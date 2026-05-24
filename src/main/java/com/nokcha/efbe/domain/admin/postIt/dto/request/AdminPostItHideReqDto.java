package com.nokcha.efbe.domain.admin.postIt.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 어드민 포스트잇 숨김 요청 — reason 선택.
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Schema(description = "어드민 포스트잇 숨김 요청 — 사유는 선택 입력 (현재는 로그용)")
public class AdminPostItHideReqDto {

    @Schema(description = "숨김 사유 (선택)",
            example = "신고 누적으로 검토 후 노출 제한",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reason;
}
