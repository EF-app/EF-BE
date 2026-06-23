package com.nokcha.efbe.domain.match.pool;

/**
 * 두 좌표 직선거리 (km) — Haversine.
 *  CandidateSelector (반경 확장) 와 ScoreCalculator.location (지역 점수) 공유.
 */
public final class GeoUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtil() {}

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                  * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
