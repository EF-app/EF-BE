package com.nokcha.efbe.domain.user.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "계정 정보 전체 조회 응답")
public class AccountRevealRspDto {

    @Schema(description = "로그인 아이디", example = "efuser01")
    private String loginId;

    @Schema(description = "이메일", example = "abcdefg@gmail.com")
    private String email;
}
