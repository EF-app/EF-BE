package com.nokcha.efbe.domain.payment.model;

/**
 * 잉크 원장 이벤트 종류. amount 는 부호 델타.
 * CHARGE — 결제 충전(+, ref=payment)  ·  USE — 아이템 사용 차감(-, ref=item)
 * REFUND — 환불 회수/복원(±)          ·  GRANT — 관리자/이벤트 지급(+)
 */
public enum InkTxType {
    CHARGE,
    USE,
    REFUND,
    GRANT
}
