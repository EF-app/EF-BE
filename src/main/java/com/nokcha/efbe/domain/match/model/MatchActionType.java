package com.nokcha.efbe.domain.match.model;

/**
 * 매칭 단방향 액션 종류
 *  - LIKE          : 좋아요. 영구 제외 (expires_at = NULL)
 *  - PASS          : 패스. 30일 쿨다운 (expires_at = NOW() + 30일)
 *  - SUPER_LIKE    : 강조 좋아요 (별 차감, 영구 제외)
 *  - POWER_MESSAGE : 메시지 먼저 보내기 (별 차감, 영구 제외)
 *
 *  ※ 한 페어당 활성 액션 1개 정책 → 신규 액션 등록 시 기존 액션 DELETE + INSERT.
 */
public enum MatchActionType {
    LIKE,
    PASS,
    SUPER_LIKE,
    POWER_MESSAGE;

    /** PASS 만 만료 (쿨다운). 나머지는 영구 제외. */
    public boolean hasCooldown() {
        return this == PASS;
    }
}
