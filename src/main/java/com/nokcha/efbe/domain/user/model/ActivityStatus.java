package com.nokcha.efbe.domain.user.model;

/**
 * 유저 접속 상태 — {@code users.last_active_at}
 *
 *  매칭피드 카드 / 좋아요 리스트 응답에서 "현재 접속 중" 등의 UI 표시 근거.
 *  계산: {@link com.nokcha.efbe.common.util.ActivityStatusResolver}
 *
 *  임계값 (분):
 *    - NOW    ≤ 10
 *    - RECENT ≤ 60
 *    - TODAY  ≤ 1,440 (24h)
 *    - OLDER  그 이상 (FE 에서 표시 X)
 */
public enum ActivityStatus {
    NOW,
    RECENT,
    TODAY,
    OLDER
}
