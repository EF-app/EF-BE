package com.nokcha.efbe.domain.user.dto.response;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

// 내 정보 요약 — 글쓰기 화면 / My 탭 등 공용
@Getter
@Builder
@Schema(description = "내 정보 요약 (닉네임 / 지역 / 나이)")
public class UserSummaryRspDto {

    @Schema(description = "닉네임", example = "민들")
    private String nickname;

    @Schema(description = "국가 (없으면 null)", example = "한국")
    private String country;

    @Schema(description = "도시 (없으면 null)", example = "서울")
    private String city;

    @Schema(description = "나이", example = "27")
    private Integer age;

    public static UserSummaryRspDto of(User user, CodeArea area) {
        return UserSummaryRspDto.builder()
                .nickname(user.getNickname())
                .country(area == null ? null : area.getCountry())
                .city(area == null ? null : area.getCity())
                .age(user.getAge())
                .build();
    }
}
