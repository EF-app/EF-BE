package com.nokcha.efbe.domain.match.dto.request;

import com.nokcha.efbe.domain.match.model.MatchActionType;
import jakarta.validation.constraints.NotNull;

public record MatchActionReqDto(
        @NotNull(message = "액션 종류는 필수입니다.")
        MatchActionType type
) {}
