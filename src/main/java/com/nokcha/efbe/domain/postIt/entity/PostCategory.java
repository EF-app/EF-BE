package com.nokcha.efbe.domain.postIt.entity;

// 포스트잇 카테고리 코드 (DDL post_it.category_code ENUM)
// LIGHTN 은 번개 카테고리 — 익명 작성 불가, 일일 한도 별도 관리
public enum PostCategory {
    LIGHTN,
    DAILY,
    LOVE,
    INFO,
    QUESTION,
    WORRY,
    FREE
}
