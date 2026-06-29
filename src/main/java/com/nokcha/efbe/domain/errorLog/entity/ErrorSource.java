package com.nokcha.efbe.domain.errorLog.entity;

/**
 * 에러 발생 출처 분류
 *
 * API       — 사용자 API 요청 중 우리 코드의 미처리 예외
 * ADMIN_API — 관리자 API 요청 중 미처리 예외
 * EXTERNAL  — 외부 의존성(R2·Firestore·FirebaseAuth·향후 PG) 호출 실패. 호출 맥락 무관
 * BATCH     — 스케줄러/배치 내부 로직 실패
 * EVENT     — @Async·이벤트 리스너 비동기 처리 실패
 * PUSH      — FCM 발송 실패 (발송 로직 구현 시 연결)
 */
public enum ErrorSource {
    API,
    ADMIN_API,
    EXTERNAL,
    BATCH,
    EVENT,
    PUSH
}
