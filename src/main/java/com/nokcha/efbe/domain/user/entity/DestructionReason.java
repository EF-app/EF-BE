package com.nokcha.efbe.domain.user.entity;

// 개인정보 파기 사유 (user_destruction_log.destruction_reason)
public enum DestructionReason {
    USER_WITHDRAW,  // 본인 탈퇴 (신청 후 30일 경과)
    DORMANT_2Y      // 장기 미이용 파기 (2년 미접속) — 휴면 배치 도입 시 사용
}
