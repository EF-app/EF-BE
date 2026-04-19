package com.efbe.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 아이디 비밀번호 입력 요청")
public class SignUpCredentialsReqDto {

    @NotBlank(message = "회원가입 토큰은 필수입니다.")
    @Schema(description = "1단계 완료 후 발급된 회원가입 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String registrationToken;

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 16, message = "아이디는 4자 이상 16자 이하로 입력해야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다.")
    @Schema(description = "로그인 아이디", example = "efuser01")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다."
    )
    @Schema(description = "로그인 비밀번호", example = "Ef123456!")
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    @Schema(description = "로그인 비밀번호 확인", example = "Ef123456!")
    private String passwordConfirm;
}
