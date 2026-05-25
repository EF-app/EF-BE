package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "투표/투표수정 요청")
public class BalVoteReqDto {

    @Schema(description = "선택지 (A 또는 B)", example = "A")
    @NotNull
    private BalVoteChoice choice;
}
