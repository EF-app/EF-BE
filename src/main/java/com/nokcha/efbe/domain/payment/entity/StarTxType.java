package com.nokcha.efbe.domain.payment.entity;

// star_transaction.tx_type — 거래 종류
public enum StarTxType {
    CHARGE,        // 충전
    USE,           // 사용
    REFUND,        // 환불
    ADMIN_GRANT    // 관리자 지급
}
