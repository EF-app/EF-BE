package com.nokcha.efbe.domain.payment.model;

/**
 * code_item 의 normal_value / palette_value 해석 방식.
 * COUNT      — 리셋 주기당 무료 허용 횟수 (-1=무제한, 0=불가). 카운터 집행 대상.
 * CAPABILITY — 능력 on/off (1=가능, 0=불가). 카운터 없음, 등급 게이트.
 * PARAM      — 기능 수치값 (예: 부스트 노출 %).
 * DURATION   — 유지 기간(일) (예: 글 TTL).
 * COOLDOWN   — 재실행 최소 경과일 (예: 닉네임/위치 변경). 타임스탬프 비교.
 */
public enum ItemValueType {
    COUNT,
    CAPABILITY,
    PARAM,
    DURATION,
    COOLDOWN
}
