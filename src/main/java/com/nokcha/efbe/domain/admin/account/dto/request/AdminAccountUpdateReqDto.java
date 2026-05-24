package com.nokcha.efbe.domain.admin.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 관리자 계정 수정 요청 — null 필드는 변경 안 함 (PATCH 시맨틱)
@Getter
@NoArgsConstructor
@Schema(description = "관리자 계정 수정 요청 — null 필드는 변경 안 함. loginId/name/비밀번호는 변경 불가 (감사·책임 추적 보존, 비밀번호는 별도 API).")
public class AdminAccountUpdateReqDto {

    @Email
    @Size(max = 100)
    @Schema(description = "업무 이메일 (null 이면 변경 안 함)",
            example = "admin@ef.test",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;

    @Schema(description = "활성 여부 — false 면 로그인 차단 (계정 삭제 대신 비활성화). null 이면 변경 안 함.",
            example = "true",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;
}
