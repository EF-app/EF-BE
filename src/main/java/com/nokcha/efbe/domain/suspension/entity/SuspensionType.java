package com.nokcha.efbe.domain.suspension.entity;

public enum SuspensionType {
    WARNING,        // 경고. users.status 미반영. 30일 내 5회 누적 시 자동 TEMPORARY 부과.
    TEMPORARY,      // 일시정지. ends_at 필수 (기간 표현). 7일/30일/임의 일수는 ends_at 으로 구분.
    PERMANENT       // 영구정지. ends_at = NULL.
}
