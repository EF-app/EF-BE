package com.nokcha.efbe.domain.user.dto.request;

import com.nokcha.efbe.domain.log.entity.LoginPlatform;
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
@Schema(description = "로그인 요청")
public class LoginReqDto {

    @NotBlank(message = "아이디는 필수입니다.")
    @Schema(description = "로그인 아이디", example = "efuser01")
    private String loginId;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(description = "로그인 비밀번호", example = "Ef123456!")
    private String password;

    @Schema(description = "기기 고유 식별자", example = "device-install-id-001")
    private String deviceId;

    @Schema(description = "로그인 플랫폼", example = "IOS")
    private LoginPlatform platform;

    @Schema(description = "보안코드 입력 단계 여부", example = "false")
    private boolean scodeStep;
}
