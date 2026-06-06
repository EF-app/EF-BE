package com.nokcha.efbe.domain.match.model;

/**
 * 선호만남. 매칭 도메인 전용 enum.
 *  우리 {@code Purpose} ↔ MatchType 매핑은 Stage 4 의 UserManagement 구현체에서 처리.
 *   - Purpose.LOVE   → LOVER
 *   - Purpose.FRIEND → FRIEND
 *   - Purpose.MIXED  → BOTH
 *
 *  명세서 §3.5: 한쪽이라도 FRIEND 면 이상형 계열 태그(#이상형/#내가/#나를) 전부 제외.
 */
public enum MatchType {
    BOTH,
    FRIEND,
    LOVER
}
