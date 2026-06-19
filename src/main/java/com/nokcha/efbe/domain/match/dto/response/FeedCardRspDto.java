package com.nokcha.efbe.domain.match.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 피드 카드 1 행 — GET /v1/matches/feed 응답 요소.
 *  read-time 오버레이 통과한 카드만 응답에 포함 (정지/탈퇴/미승인/차단 자동 제외).
 *
 *  ── 표시 데이터 ──
 *    nickname/age/location/mbti/job/bioMessage/mainPhotoUrl/distanceKm 은 실시간 join — 본인이 닉네임·사진·지역
 *    등을 바꾸면 즉시 반영
 *  ── 매칭 데이터 ──
 *    rank/slotType/tagsJson 은 마지막 배치 결과 — 점수/태그 %는 어제 기준 (eventual consistency).
 *
 *  location 은 country + city 를 합친 표시 문자열 (예: "한국 서울"). null 가능.
 *  distanceKm 은 viewer ↔ target 의 ST_Distance_Sphere 결과 (km). 한쪽이라도 좌표 없으면 null.
 */
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
}
