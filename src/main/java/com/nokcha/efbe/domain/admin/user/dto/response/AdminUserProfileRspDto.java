package com.nokcha.efbe.domain.admin.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 어드민 유저 상세 — 프로필 패널 전체 데이터.
// user_profile + user_keyword + user_custom_keyword + user_personal
// 음주/흡연/타투/스타일/이상형 값은 code_personal 의 라벨 텍스트 그대로 노출.
@Getter
@Builder
@Schema(description = "어드민 유저 프로필 패널 — 프로필/관심사/생활습관/스타일/이상형 + 심사 상태")
public class AdminUserProfileRspDto {

    @Schema(description = "MBTI", example = "ENFP", nullable = true)
    private String mbti;

    @Schema(description = "매칭 목적", example = "LOVE", nullable = true)
    private String matchPurpose;

    @Schema(description = "관심 대상", example = "ALL", nullable = true)
    private String interestTarget;

    @Schema(description = "직업", example = "디자이너", nullable = true)
    private String job;

    @Schema(description = "한 줄 소개 / 자기소개", example = "조용한 산책을 좋아해요", nullable = true)
    private String bioMessage;

    @Schema(description = "이상형 포인트 타입 목록", example = "[\"OUTGOING\", \"ROMANTIC\"]", nullable = true)
    private List<String> idealPoints;

    @Schema(description = "관심사 키워드 — 그룹키(lifestyle/hobby/outdoor/self_improve/food/sports/music/game) → 라벨 목록",
            example = "{\"hobby\": [\"독서\", \"영화\"], \"food\": [\"한식\"]}", nullable = true)
    private Map<String, List<String>> keywords;

    @Schema(description = "사용자 커스텀 태그 목록", example = "[\"빵순이\", \"INFJ\"]", nullable = true)
    private List<String> myTags;

    @Schema(description = "음주 습관", example = "가끔", nullable = true)
    private String drinking;

    @Schema(description = "선호 주종 목록", example = "[\"와인\", \"맥주\"]", nullable = true)
    private List<String> drinkTypes;

    @Schema(description = "흡연 습관", example = "비흡연", nullable = true)
    private String smoking;

    @Schema(description = "흡연 종류 목록", example = "[\"전자담배\"]", nullable = true)
    private List<String> smokeTypes;

    @Schema(description = "타투 여부/스타일", example = "없음", nullable = true)
    private String tattoo;

    @Schema(description = "내 헤어 스타일", example = "단발", nullable = true)
    private String hairStyle;

    @Schema(description = "내 체형", example = "보통", nullable = true)
    private String bodyType;

    @Schema(description = "내 키", example = "172cm", nullable = true)
    private String height;

    @Schema(description = "분위기", example = "차분한", nullable = true)
    private String vibe;

    @Schema(description = "데일리 타입 — 아침형/저녁형", example = "저녁형", nullable = true)
    private String dailyType;

    @Schema(description = "종교", example = "무교", nullable = true)
    private String religion;

    @Schema(description = "주변 친구 분위기", example = "조용한 편", nullable = true)
    private String friendsAround;

    @Schema(description = "커밍아웃 단계", example = "비공개", nullable = true)
    private String comingOut;

    @Schema(description = "패션 스타일", example = "캐주얼", nullable = true)
    private String fashion;

    @Schema(description = "꾸밈 정도", example = "심플", nullable = true)
    private String grooming;

    @Schema(description = "이상형 헤어 스타일", example = "긴 머리", nullable = true)
    private String idealHair;

    @Schema(description = "이상형 체형", example = "마른", nullable = true)
    private String idealBody;

    @Schema(description = "이상형 키", example = "175cm 이상", nullable = true)
    private String idealHeight;

    @Schema(description = "이상형 분위기", example = "발랄한", nullable = true)
    private String idealVibe;

    @Schema(description = "프로필 심사 상태", example = "APPROVED", nullable = true)
    private String profileStatus;

    @Schema(description = "프로필 반려 사유 (REJECTED 일 때만)", example = "사진 식별 불가", nullable = true)
    private String profileRejectedReason;

    @Schema(description = "프로필 심사 일시", example = "2026-05-22T11:00:00", nullable = true)
    private LocalDateTime profileReviewedAt;

    @Schema(description = "심사 처리한 관리자 PK", example = "3", nullable = true)
    private Long profileReviewedBy;
}
