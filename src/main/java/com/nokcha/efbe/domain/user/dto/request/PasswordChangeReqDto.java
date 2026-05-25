package com.nokcha.efbe.domain.user.dto.request;

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
@Schema(description = "비밀번호 변경 요청")
public class PasswordChangeReqDto {

    @NotBlank(message = "기존 비밀번호는 필수입니다.")
    @Schema(description = "현재 비밀번호", example = "Ef123456!")
    private String oldPassword;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해 8자 이상이어야 합니다."
    )
    @Schema(description = "새 비밀번호", example = "Ef654321!")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    @Schema(description = "새 비밀번호 확인", example = "Ef654321!")
    private String newPasswordConfirm;
}
