package com.nokcha.efbe.domain.admin.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 관리자 계정 생성 요청
@Getter
@NoArgsConstructor
@Schema(description = "관리자 계정 생성 요청 — 시스템>관리자계정 화면 [추가]")
public class AdminAccountCreateReqDto {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "로그인 아이디 (uk_admin_login_id 중복 시 409)",
            example = "admin01",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String loginId;

    @NotBlank
    @Size(min = 8, max = 64)
    @Schema(description = "초기 비밀번호 — 평문 전달, BE 에서 bcrypt 해시 후 저장",
            example = "TempPass!234",
            minLength = 8,
            maxLength = 64,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "관리자 실명 (감사·책임 추적용)",
            example = "홍길동",
            maxLength = 50,
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Email
    @Size(max = 100)
    @Schema(description = "업무 이메일 (옵션)",
            example = "admin@ef.test",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String email;
}
