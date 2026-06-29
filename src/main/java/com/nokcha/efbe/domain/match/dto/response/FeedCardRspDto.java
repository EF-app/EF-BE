package com.nokcha.efbe.domain.match.dto.response;

import com.nokcha.efbe.domain.user.model.ActivityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "피드 카드 1행 — read-time 오버레이 통과한 카드만 포함")
public class FeedCardRspDto {

    @Schema(description = "노출 순위 (1~50)")
    private int matchRank;

    @Schema(description = "카드 대상 사용자 id")
    private long targetId;

    @Schema(description = "슬롯 타입 (SCORE/NEWBIE/RANDOM/CUSTOM_KW/FRESH_NEWBIE)")
    private String slotType;

    @Schema(description = "표시용 태그 JSON 문자열 (배열)")
    private String tagsJson;

    @Schema(description = "닉네임 — 실시간 join")
    private String nickname;

    @Schema(description = "나이")
    private Integer age;

    @Schema(description = "지역 표시 문자열 (country + city). null 가능")
    private String location;

    @Schema(description = "MBTI")
    private String mbti;

    @Schema(description = "직업 코드")
    private String job;

    @Schema(description = "자기소개 메시지")
    private String bioMessage;

    @Schema(description = "대표 사진 URL (sort_order 가장 낮은 사진). 사진 없으면 null")
    private String mainPhotoUrl;

    @Schema(description = "viewer ↔ target 거리(km). 한쪽이라도 좌표 없으면 null")
    private Double distanceKm;

    @Schema(description = "접속 상태 (NOW ≤10분 / RECENT ≤60분 / TODAY ≤24h / OLDER). FE 는 OLDER 를 숨김 처리")
    private ActivityStatus activityStatus;
}
