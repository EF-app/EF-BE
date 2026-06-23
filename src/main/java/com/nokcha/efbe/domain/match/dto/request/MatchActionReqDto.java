package com.nokcha.efbe.domain.match.dto.request;

import com.nokcha.efbe.domain.match.model.MatchActionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchActionReqDto {

    @NotNull(message = "액션 종류는 필수입니다.")
    private MatchActionType type;
}
