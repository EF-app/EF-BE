package com.nokcha.efbe.domain.match.model;

/**
 * 이상형 중요 포인트 — sortKey 가중치 가산 + ⭐ 강조에만 영향.
 *  명세서 §2.5 / §1.2 참고.
 *
 *  우리 {@code IdealPointType} ↔ ImportantPoint 매핑 (Stage 4 UserManagement 가 처리):
 *   - KEYWORD     → KEYWORD
 *   - IDEAL_TYPE  → IDEAL
 *   - LIFE_STYLE  → LIFESTYLE
 *   - AREA        → LOCATION
 *
 *  ※ 가산값은 영역별로 다름 (명세서 §2.5 차등 가산):
 *    bumpKeyword(0.15), bumpIdeal(0.20), bumpLifestyle(0.05), bumpLocation(0.05).
 *    잘 안 오르는 영역(키워드·이상형)에 더 큰 가산을 주어 사용자의 강조 의도를 반영.
 */
public enum ImportantPoint {
    KEYWORD,
    IDEAL,
    LIFESTYLE,
    LOCATION
}
