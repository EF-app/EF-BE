package com.nokcha.efbe.domain.admin.balGame.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 어드민이 BAL-APPLY 를 거절할 때 사유 메모.
@Getter
@NoArgsConstructor
public class AdminBalApplyRejectReqDto {

    @Schema(description = "거절 사유 (admin_memo 에 저장, 신청자에게 통보 가능)", maxLength = 255)
    @Size(max = 255)
    private String adminMemo;
}
