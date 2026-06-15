package com.nokcha.efbe.domain.match.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;

/**
 * 매칭 도메인 공용 유틸 — 의사코드 stub(intersect/union/jaccard/toJson/pct) 실제 구현.
 *  사용처: ScoreCalculator 5종, MatchCalculator, TagDisplayFormatter.
 */
public final class MatchUtil {

    private static final ObjectMapper OM = new ObjectMapper();

    private MatchUtil() {}

    /** 교집합 — null/empty 안전 (빈 Set 반환). */
    public static <T> Set<T> intersect(Set<T> a, Set<T> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return Set.of();
        Set<T> result = new HashSet<>(a);
        result.retainAll(b);
        return result;
    }

    /** 합집합 — null 안전. */
    public static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new HashSet<>(a == null ? Set.of() : a);
        if (b != null) result.addAll(b);
        return result;
    }

    /** Jaccard 유사도 = |A∩B| / |A∪B|. 한쪽이라도 비면 0.0. */
    public static <T> double jaccard(Set<T> a, Set<T> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        int inter = intersect(a, b).size();
        int uni   = union(a, b).size();
        return uni == 0 ? 0.0 : (double) inter / uni;
    }

    /** 점수 0~1 → 정수 % (반올림). */
    public static int pct(double score) {
        return (int) Math.round(score * 100);
    }

    /** 객체 → JSON 문자열. 실패 시 RuntimeException (배치 흐름 중단). */
    public static String toJson(Object value) {
        try {
            return OM.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("match toJson 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 단계형 거리 점수 (stepDistance).
     *  d=0:1.0, d=1:0.6, d=2:0.3, d=3:0.1, d≥4:0.0
     */
    public static double stepDistance(int idxA, int idxB) {
        return switch (Math.abs(idxA - idxB)) {
            case 0 -> 1.0;
            case 1 -> 0.6;
            case 2 -> 0.3;
            case 3 -> 0.1;
            default -> 0.0;
        };
    }
}
