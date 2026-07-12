package com.nokcha.efbe.domain.payment.model;

/**
 * 팔레트(구독) 생애주기 이벤트.
 * START — 신규 시작  ·  EXTEND — 연장/갱신  ·  CANCEL — 자동갱신 해지
 * EXPIRE — 만료       ·  GIFT — 무료/관리자 지급
 */
public enum PaletteEventType {
    START,
    EXTEND,
    CANCEL,
    EXPIRE,
    GIFT
}
