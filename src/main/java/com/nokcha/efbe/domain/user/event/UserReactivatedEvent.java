package com.nokcha.efbe.domain.user.event;

/**
 * 휴면/제재/탈퇴 상태에서 ACTIVE 로 복귀 — 매칭 본인 피드 즉시 재계산 트리거.
 *
 *  발행처:
 *    - 휴면 복귀 (DORMANT_RECOVERED)    : {@code UserAuthService.login} — last_active_at 갱신 전 값이 31일 초과
 *    - 탈퇴 취소 (WITHDRAW_CANCELLED)   : {@code UserInfoService.cancelWithdrawal} — status WITHDRAWING → ACTIVE
 *    - 제재 해제 (SUSPENSION_LIFTED)    : {@code SuspensionExpirationScheduler} (자동) / {@code AdminSuspensionService.lift} (수동)
 *
 *  수신:
 *    {@code MatchFeedRecomputeListener.onUserReactivated} — AFTER_COMMIT + @Async + MyFeedRecomputer.recompute
 *    본인 프로필 이미 존재 + 자격 풀 정상 → ColdStart 가 아닌 정상 매칭 사용.
 *    안전망: process 내부의 풀 0명 fallback 이 ColdStart 자동 호출.
 *
 *  ※ 자동 트리거라 어뷰즈 통로 X. 어뷰즈 가드 (액션/일일횟수) 적용 안 함.
 */
public record UserReactivatedEvent(long userId, Reason reason) {

    public enum Reason {
        DORMANT_RECOVERED,    // 휴면 → ACTIVE (31일 초과 후 로그인)
        WITHDRAW_CANCELLED,   // 탈퇴취소 → ACTIVE (WITHDRAWING → ACTIVE)
        SUSPENSION_LIFTED     // 제재해제 → ACTIVE (TEMPORARY → ACTIVE, 자동/수동 모두)
    }
}
