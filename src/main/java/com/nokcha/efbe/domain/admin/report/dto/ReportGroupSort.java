package com.nokcha.efbe.domain.admin.report.dto;

/** 신고 그룹 목록 정렬 옵션. */
public enum ReportGroupSort {
    /** 최신 신고 우선 (그룹의 마지막 신고 시각 DESC) */
    LATEST,
    /** 첫 신고 오래된 순 (기본 — 처리 우선순위) */
    OLDEST,
    /** 신고 건수 많은 순, 동률이면 오래된 순 */
    MOST_REPORTED
}
