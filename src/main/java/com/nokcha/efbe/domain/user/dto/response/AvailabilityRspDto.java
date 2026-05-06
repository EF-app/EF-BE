package com.nokcha.efbe.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "값 사용 가능 여부 응답 (아이디/닉네임 중복 체크)")
public class AvailabilityRspDto {

    @Schema(description = "사용 가능 여부 (true=중복 없음, false=이미 사용 중)", example = "true")
    private boolean available;

    public static AvailabilityRspDto of(boolean available) {
        return AvailabilityRspDto.builder().available(available).build();
    }
}
