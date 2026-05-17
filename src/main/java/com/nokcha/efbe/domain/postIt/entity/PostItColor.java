package com.nokcha.efbe.domain.postIt.entity;

// 포스트잇 색상 슬롯 (DDL post_it.color VARCHAR(16) + CHECK)
// 추상 코드 → FE 가 슬롯별 hex 매핑 (디자인 시스템 변경 시 BE 무변경)
// 색상 추가 시: enum 값 추가 + DB chk_post_color 제약 갱신
public enum PostItColor {
    P1,  // #EEE9F6
    P2,  // #E4DEF2
    P3,  // #F6F3FB
    P4,  // #E8E0EF
    P5   // #F2EDF6
}
