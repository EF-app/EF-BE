package com.nokcha.efbe.domain.match.model;

/**
 * 매칭 성사 트리거 종류 (match_results.trigger_type).
 *  - MUTUAL_LIKE        : 양방향 LIKE 매칭 (매칭피드 ❤ / 받은좋아요 ❤ 둘 다 포함)
 *  - PRE_MESSAGE_REPLY  : 파워메시지 답장 → 자동 매칭
 *  - PRE_MESSAGE_LIKE   : 파워메시지에 좋아요 응답 → 자동 매칭 -- 보류
 *
 *  SUPER_LIKE 매칭은 별도 분리하지 않음 — match_results.is_super 플래그로 표시.
 */
public enum MatchTriggerType {
    MUTUAL_LIKE,
    PRE_MESSAGE_REPLY,
    PRE_MESSAGE_LIKE
}
