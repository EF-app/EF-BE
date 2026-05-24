package com.nokcha.efbe.domain.block.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 유저 차단 요청 DTO
@Getter
@NoArgsConstructor
@Schema(description = "유저 차단 요청")
public class BlockCreateReqDto {

    @NotNull
    @Schema(description = "차단할 유저 id", example = "102")
    private Long blockedUserId;
}
