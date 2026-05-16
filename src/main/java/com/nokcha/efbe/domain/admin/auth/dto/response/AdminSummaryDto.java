package com.nokcha.efbe.domain.admin.auth.dto.response;

import com.nokcha.efbe.domain.admin.auth.entity.AdminAccount;
import io.swagger.v3.oas.annotations.media.Schema;

// 내 정보 조회 DTO.
@Schema(description = "관리자 정보")
public record AdminSummaryDto(
        @Schema(description = "외부 노출용 UUID") String uuid,
        @Schema(description = "로그인 아이디", example = "admin01") String loginId,
        @Schema(description = "관리자 이름", example = "홍길동") String name,
        @Schema(description = "관리자 권한", example = "ADMIN") String role
) {
    public static AdminSummaryDto from(AdminAccount account) {
        return new AdminSummaryDto(
                account.getUuid(),
                account.getLoginId(),
                account.getName(),
                account.getRole().name()
        );
    }
}
