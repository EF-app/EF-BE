package com.nokcha.efbe.domain.block.dto.request;

import com.nokcha.efbe.domain.block.entity.BlockReasonCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @Schema(description = "차단 사유 카테고리. 생략 시 OTHER", example = "PROFANITY_HATE")
    private BlockReasonCategory reasonCategory;

    @Size(max = 500)
    @Schema(description = "상세 사유 (선택, 최대 500자)")
    private String detail;
}
