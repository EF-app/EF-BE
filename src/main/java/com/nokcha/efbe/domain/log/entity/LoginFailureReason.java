package com.nokcha.efbe.domain.log.entity;

public enum LoginFailureReason {
    INVALID_PASSWORD,
    INVALID_ID,
    SUSPENDED,
    WITHDRAWN,
    SCODE_FAIL,
    OTHER
}
