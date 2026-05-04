package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원가입 옵션 그룹")
public class SignUpOptionGroupRspDto {

    @Schema(description = "옵션 카테고리", example = "음주")
    private String category;

    @ArraySchema(schema = @Schema(implementation = SignUpOptionItemRspDto.class))
    private List<SignUpOptionItemRspDto> options;
}
