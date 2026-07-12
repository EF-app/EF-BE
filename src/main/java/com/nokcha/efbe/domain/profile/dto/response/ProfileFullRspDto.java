package com.nokcha.efbe.domain.profile.dto.response;

import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Mbti;
import com.nokcha.efbe.domain.profile.entity.Purpose;
import com.nokcha.efbe.domain.user.entity.Job;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "프로필 수정 화면 진입용 풀 조회 응답 — 8개 섹션 + 사진 + 기본정보")
public class ProfileFullRspDto {

    @Schema(description = "닉네임", example = "민들")
    private String nickname;

    @Schema(description = "지역 PK", example = "12", nullable = true)
    private Long areaId;

    @Schema(description = "국가 (display 용)", example = "한국", nullable = true)
    private String country;

    @Schema(description = "도시 (display 용)", example = "서울", nullable = true)
    private String city;

    @Schema(description = "나이", example = "27", nullable = true)
    private Integer age;

    /* 사진 */
    @Schema(description = "프로필 사진 목록 (sortOrder asc)")
    private List<PhotoItem> photos;

    /* user_profile */
    @Schema(description = "관심 대상", example = "BOTH", nullable = true)
    private Purpose purpose;

    @Schema(description = "MBTI", example = "ENFP", nullable = true)
    private Mbti mbti;

    @Schema(description = "직업", example = "OFFICE_WORKER", nullable = true)
    private Job job;

    @Schema(description = "이상형 중요 포인트", nullable = true)
    private List<IdealPointType> idealPointTypes;

    @Schema(description = "자기소개", nullable = true)
    private String bioMessage;

    /* 키워드 */
    @Schema(description = "선택된 추천 키워드 ID 목록")
    private List<Long> keywordIds;

    @Schema(description = "선택된 나만의 태그 목록")
    private List<String> customKeywords;

    /* personal */
    @Schema(description = "SELF 성향 personalId 목록 (lifestyle + my-style)")
    private List<Long> selfPersonalIds;

    @Schema(description = "IDEAL 성향 personalId 목록 (이상형)")
    private List<Long> idealPersonalIds;

    @Getter
    @Builder
    @Schema(description = "프로필 사진 항목")
    public static class PhotoItem {
        @Schema(description = "사진 PK", example = "302")
        private Long id;

        @Schema(description = "사진 URL", example = "https://cdn.example.com/users/42/profile-0.jpg")
        private String url;

        @Schema(description = "정렬 순서 (낮을수록 대표)", example = "0")
        private Integer sortOrder;
    }
}
