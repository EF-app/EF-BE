package com.nokcha.efbe.domain.admin.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 로그인 요청")
public class AdminLoginReqDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "로그인 아이디", example = "admin01")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "로그인 비밀번호", example = "Admin1234!")
    private String password;
}
