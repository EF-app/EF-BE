package com.nokcha.efbe.domain.user.account.dto.request;

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
@Schema(description = "계정 정보 전체 조회 요청 (비밀번호 재인증)")
public class AccountRevealReqDto {

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호", example = "Ef123456!")
    private String password;
}
