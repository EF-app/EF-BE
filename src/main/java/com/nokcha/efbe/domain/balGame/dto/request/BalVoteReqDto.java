package com.nokcha.efbe.domain.balGame.dto.request;

import com.nokcha.efbe.domain.balGame.entity.BalVoteChoice;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 투표/투표수정 요청 DTO
@Getter
@NoArgsConstructor
public class BalVoteReqDto {

    @NotNull
    private BalVoteChoice choice;
}
