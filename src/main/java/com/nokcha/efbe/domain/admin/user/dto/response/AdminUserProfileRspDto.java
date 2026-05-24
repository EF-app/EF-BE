package com.nokcha.efbe.domain.admin.user.dto.response;

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
public class AdminUserProfileRspDto {

    private String mbti;
    private String matchPurpose;        // user_profile.purpose (LOVE/FRIEND/MIXED)
    private String interestTarget;      // purpose 기반 — ACQUAINTANCE/ALL/LOVER
    private String job;
    private String bioMessage;          // user_profile.message
    private List<String> idealPoints;   // user_profile.ideal_point_types

    // 관심사 키워드 — 그룹키(lifestyle/hobby/outdoor/self_improve/food/sports/music/game) → 라벨 목록
    private Map<String, List<String>> keywords;
    private List<String> myTags;        // user_custom_keyword

    // 생활 습관 (user_personal type=SELF)
    private String drinking;
    private List<String> drinkTypes;
    private String smoking;
    private List<String> smokeTypes;
    private String tattoo;

    // 내 스타일 (user_personal type=SELF)
    private String hairStyle;
    private String bodyType;
    private String height;
    private String vibe;
    private String dailyType;
    private String religion;
    private String friendsAround;
    private String comingOut;
    private String fashion;
    private String grooming;

    // 이상형 (user_personal type=IDEAL)
    private String idealHair;
    private String idealBody;
    private String idealHeight;
    private String idealVibe;

    // 프로필 심사 상태 (관리자용)
    private String profileStatus;
    private String profileRejectedReason;
    private LocalDateTime profileReviewedAt;
    private Long profileReviewedBy;
}
