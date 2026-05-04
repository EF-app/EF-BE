package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 옵션 항목")
public class SignUpOptionItemRspDto {

    @Schema(description = "코드 테이블 ID", example = "1", nullable = true)
    private Long id;

    @Schema(description = "enum 코드값", example = "OFFICE_WORKER", nullable = true)
    private String code;

    @Schema(description = "화면 표시명", example = "직장인")
    private String label;
}
