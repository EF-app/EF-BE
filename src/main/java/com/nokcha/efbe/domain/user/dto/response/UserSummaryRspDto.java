package com.nokcha.efbe.domain.user.dto.response;

import com.nokcha.efbe.domain.area.entity.CodeArea;
import com.nokcha.efbe.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "내 정보 요약 (닉네임 / 지역 / 나이 / 대표 사진)")
public class UserSummaryRspDto {

    @Schema(description = "닉네임", example = "민들")
    private String nickname;

    @Schema(description = "국가 (없으면 null)", example = "한국")
    private String country;

    @Schema(description = "도시 (없으면 null)", example = "서울")
    private String city;

    @Schema(description = "나이", example = "27")
    private Integer age;

    @Schema(description = "대표 프로필 사진 URL (sortOrder 가장 낮은 사진). 없으면 null",
            example = "https://cdn.example.com/users/42/profile-0.jpg")
    private String profileImageUrl;

    public static UserSummaryRspDto of(User user, CodeArea area, String profileImageUrl) {
        return UserSummaryRspDto.builder()
                .nickname(user.getNickname())
                .country(area == null ? null : area.getCountry())
                .city(area == null ? null : area.getCity())
                .age(user.getAge())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
