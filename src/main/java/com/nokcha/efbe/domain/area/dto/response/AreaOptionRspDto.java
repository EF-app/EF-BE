package com.nokcha.efbe.domain.area.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 지역(code_area) 옵션")
public class AreaOptionRspDto {

    @Schema(description = "code_area PK", example = "1")
    private Long id;

    @Schema(description = "시/도", example = "서울특별시")
    private String country;

    @Schema(description = "시/군/구", example = "강남구")
    private String city;
}
