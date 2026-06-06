package com.nokcha.efbe.domain.match.dto.response;

/**
 * 피드 카드 1 행 — GET /v1/matches/feed 응답 요소.
 *  read-time 오버레이 통과한 카드만 응답에 포함 (정지/탈퇴/미승인/차단 자동 제외).
 *
 *  ── 표시 데이터 ──
 *    nickname/age/location/mbti/job/bioMessage/mainPhotoUrl/distanceKm 은 실시간 join — 본인이 닉네임·사진·지역
 *    등을 바꾸면 즉시 반영 (배치 무관, 명세서 §8.3 참고).
 *  ── 매칭 데이터 ──
 *    rank/slotType/tagsJson 은 마지막 배치 결과 — 점수/태그 %는 어제 기준 (eventual consistency).
 *
 *  location 은 country + city 를 합친 표시 문자열 (예: "한국 서울"). null 가능.
 *  distanceKm 은 viewer ↔ target 의 ST_Distance_Sphere 결과 (km). 한쪽이라도 좌표 없으면 null.
 */
public record FeedCardRspDto(
        int rank,
        long targetId,
        String slotType,
        String tagsJson,
        String nickname,
        Integer age,
        String location,
        String mbti,
        String job,
        String bioMessage,
        String mainPhotoUrl,
        Double distanceKm
) {}
