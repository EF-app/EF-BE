package com.nokcha.efbe.domain.match.model;

import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Purpose;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

/**
 * 매칭이 보는 유저 단면.
 *  우리 {@code User} + {@code UserProfile} + {@code UserKeyword} + {@code UserCustomKeyword}
 *  + {@code UserPersonal} + {@code CodeArea} → 이 record 로 매핑.
 *  매핑은 통합 지점 1 ({@code UserManagementImpl}) 의 단일 책임.
 *
 *  ※ 모든 계산기 / 풀 / 슬롯 / 태그 판정은 이 record 만 본다.
 */
public record UserContext(
        long id,
        int age,
        LocalDate signupAt,
        /** "한국" / "해외" 등 — 국내/해외 그룹 분리 키 */
        String regionCountry,
        double lat,
        double lon,
        Purpose purpose,

        /** 전체 키워드 (9개 카테고리 통합) */
        Set<String> keywords,
        /** 개인 키워드 (✨#개인키워드 태그용) */
        Set<String> customKeywords,
        /** 카테고리코드 → 키워드 (같은카테고리 OUTDOOR/SELF_DEV/SPORTS 용) */
        Map<String, Set<String>> keywordsByCategory,

        Ideal ideal,
        Self self,

        Drinking drinking,
        Smoking smoking,

        /** 이상형 중요 포인트 — sortKey 가중치 차등 가산 + ⭐ 강조용 */
        Set<IdealPointType> importantPoints
) {}
