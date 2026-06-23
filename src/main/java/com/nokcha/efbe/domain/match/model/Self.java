package com.nokcha.efbe.domain.match.model;

import java.util.Set;

/**
 * 본인 스타일 — 가입 시 입력한 단일/다중 선택값.
 *  fashion 만 Set, 나머지는 단일.
 *  필드별 null 처리: 본인이 미입력해도 비교 가능하도록 null 허용 (이상형 비교 시 fallback).
 */
public record Self(
        HairLength hair,
        BodyType body,
        HeightBand height,
        Tendency tendency,
        Set<Fashion> fashion,
        Grooming grooming
) {}
