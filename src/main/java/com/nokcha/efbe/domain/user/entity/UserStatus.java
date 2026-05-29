package com.nokcha.efbe.domain.user.entity;

public enum UserStatus {
    ACTIVE,             // 정상. 모든 기능 이용 가능
    TEMPORARY,          // 일시정지 (user_suspension.is_lifted=false AND ends_at > NOW 으로 활성 판정)
    PERMANENT,          // 영구정지
    WITHDRAWING,        // 탈퇴 신청 (30일 유예 기간)
    WITHDRAWN           // 탈퇴 완료
}
