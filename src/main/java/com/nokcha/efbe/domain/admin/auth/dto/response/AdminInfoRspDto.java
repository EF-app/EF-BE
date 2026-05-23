package com.nokcha.efbe.domain.admin.auth.dto.response;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "관리자 정보")
@Getter
@Builder
public class AdminInfoRspDto {
    @Schema(description = "로그인 아이디", example = "admin")
    private String loginId;

    @Schema(description = "관리자 이름", example = "홍길동")
    private String name;

    public static AdminInfoRspDto from(AdminAccount account) {
        return AdminInfoRspDto.builder()
                .loginId(account.getLoginId())
                .name(account.getName())
                .build();
    }
}