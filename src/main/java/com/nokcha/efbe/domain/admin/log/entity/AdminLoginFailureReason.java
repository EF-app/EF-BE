package com.nokcha.efbe.domain.admin.log.entity;

public enum AdminLoginFailureReason {
    INVALID_PASSWORD,
    INVALID_ID,
    ACCOUNT_INACTIVE,
    ACCOUNT_LOCKED,
    IP_NOT_ALLOWED,
    TOTP_FAILED,
    OTHER
}
