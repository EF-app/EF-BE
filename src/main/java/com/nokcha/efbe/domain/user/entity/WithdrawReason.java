package com.nokcha.efbe.domain.user.entity;

public enum WithdrawReason {
    NO_MATCH,   // 매칭 없음
    FOUND_PARTNER,  // 파트너 찾음
    OTHER_APP,  // 타앱 이동
    PRIVACY_CONCERN,    // 개인정보 우려
    TOO_EXPENSIVE,  // 가격
    BAD_USERS,  // 불량 유저
    BUG_OR_ISSUE,   // 버그
    TAKING_BREAK,   // 휴식
    ETC
}