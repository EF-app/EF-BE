package com.nokcha.efbe.domain.match.repository;

import com.nokcha.efbe.domain.match.model.ImportantPoint;
import com.nokcha.efbe.domain.match.model.MatchType;
import com.nokcha.efbe.domain.profile.entity.IdealPointType;
import com.nokcha.efbe.domain.profile.entity.Purpose;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 통합 지점 1 보조 — 우리 엔티티 enum ↔ 매칭 도메인 enum 변환.
 *  의도적으로 단방향만 정의 (도메인 → 매칭). 매칭 결과를 다시 엔티티로 쓰는 경로는 없다.
 *
 *  매핑 근거 (이전 세션 사용자 확정):
 *    Purpose         → MatchType         : MIXED→BOTH, FRIEND→FRIEND, LOVE→LOVER
 *    IdealPointType  → ImportantPoint    : KEYWORD→KEYWORD,
 *                                          IDEAL_TYPE→IDEAL,
 *                                          LIFE_STYLE→LIFESTYLE,
 *                                          AREA→LOCATION
 */
public final class MatchEnumMapper {

    private static final Map<Purpose, MatchType> PURPOSE_TO_MATCH = new EnumMap<>(Purpose.class);
    private static final Map<IdealPointType, ImportantPoint> IDEAL_TO_IMPORTANT = new EnumMap<>(IdealPointType.class);

    static {
        PURPOSE_TO_MATCH.put(Purpose.MIXED,  MatchType.BOTH);
        PURPOSE_TO_MATCH.put(Purpose.FRIEND, MatchType.FRIEND);
        PURPOSE_TO_MATCH.put(Purpose.LOVE,   MatchType.LOVER);

        IDEAL_TO_IMPORTANT.put(IdealPointType.KEYWORD,    ImportantPoint.KEYWORD);
        IDEAL_TO_IMPORTANT.put(IdealPointType.IDEAL_TYPE, ImportantPoint.IDEAL);
        IDEAL_TO_IMPORTANT.put(IdealPointType.LIFE_STYLE, ImportantPoint.LIFESTYLE);
        IDEAL_TO_IMPORTANT.put(IdealPointType.AREA,       ImportantPoint.LOCATION);
    }

    private MatchEnumMapper() {}

    public static MatchType toMatchType(Purpose p) {
        return p == null ? MatchType.BOTH : PURPOSE_TO_MATCH.getOrDefault(p, MatchType.BOTH);
    }

    public static Set<ImportantPoint> toImportantPoints(java.util.List<IdealPointType> list) {
        if (list == null || list.isEmpty()) return Set.of();
        return list.stream()
                .map(IDEAL_TO_IMPORTANT::get)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
    }
}
