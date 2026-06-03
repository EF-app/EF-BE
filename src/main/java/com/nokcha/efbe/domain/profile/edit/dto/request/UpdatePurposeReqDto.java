package com.nokcha.efbe.domain.profile.edit.dto.request;

import com.nokcha.efbe.domain.profile.entity.Purpose;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "관심 대상(purpose) 수정 요청")
public class UpdatePurposeReqDto {

    @NotNull
    @Schema(description = "관심 대상 — ACQUAINTANCE/LOVE/BOTH", example = "BOTH")
    private Purpose purpose;
}
