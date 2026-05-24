package com.nokcha.efbe.domain.profile.entity;

/**
 * 프로필 심사 상태.
 *   - 신규 가입 시 자동 APPROVED.
 *   - 관리자만 APPROVED → REJECTED, REJECTED → APPROVED 양방향 토글 가능.
 *   - APPROVED : 매칭 피드 정상 노출. match_score_cache 계산 대상.
 *   - REJECTED : 관리자가 사유와 함께 반려. 유저에게 사유 안내 후 재제출 요청.
 */
public enum ProfileStatus {
    PENDING,
    APPROVED,
    REJECTED
}
