package com.nokcha.efbe.domain.admin.auth.entity;

// 관리자 로그인 실패 사유 (DDL admin_login_log.failure_reason ENUM).
public enum AdminLoginFailureReason {
    INVALID_PASSWORD,
    INVALID_ID,
    ACCOUNT_INACTIVE,
    ACCOUNT_LOCKED,
    IP_NOT_ALLOWED,
    TOTP_FAILED,
    OTHER
}
