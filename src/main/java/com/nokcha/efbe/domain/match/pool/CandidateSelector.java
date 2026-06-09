package com.nokcha.efbe.domain.match.pool;

import com.nokcha.efbe.domain.match.config.MatchingConfig;
import com.nokcha.efbe.domain.match.model.UserContext;
import com.nokcha.efbe.domain.match.repository.UserManagement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 1단계 후보 생성 (명세서 §4.2): 자격 풀(하드필터) → 뉴비/기존 두 양동이 → 반경 확장 → 백필 → poolSize.
 *  - 반경 필터는 메모리 Haversine. 트래픽 커지면 SQL 바운딩박스 + 좌표 인덱스로 이전.
 *  - "해외" 그룹은 반경 무한대 동일 그룹 안에서만 섞임 (그룹 일치는 UserManagement 의 SQL 단계에서).
 */
@Component
@RequiredArgsConstructor
public class CandidateSelector {

    private final UserManagement userMgmt;

    public List<UserContext> buildPool(UserContext me, MatchingConfig cfg) {
        return buildPool(me, cfg, null);
    }

    /**
     *  - findEligibleIds: 하드필터 SQL + bbox 좌표 인덱스 → bbox 안 후보 전체 ID 반환
     *
     *  Collections.shuffle 로 메모리 셔플 — DB 가 컷 안 하므로 무작위 pkId.
     *  bbox 안 후보가 풀 size(500) 보다 클 때 fillBucket/backfill 이 무작위 순서로 풀 채움.
     */
    public List<UserContext> buildPool(UserContext me, MatchingConfig cfg,
                                       Map<Long, UserContext> activeCache) {
        List<UserContext> eligible;
        if (activeCache != null) {
            // bbox 100km — radiusStepsKm 의 마지막 단계(전국=-1) 직전.
            //  국내 그룹은 이 bbox 안에서 풀 500 채우면 충분. 부족하면 백필이 ColdStartFeed 로 처리
            int radiusKm = 100;
            List<Long> ids = userMgmt.findEligibleIds(me, cfg, radiusKm);
            eligible = new ArrayList<>(ids.size());
            for (Long id : ids) {
                UserContext u = activeCache.get(id);
                if (u != null) eligible.add(u);
            }
        } else {
            // 단건 호출 (MyFeedRecomputer.recompute) — 캐시 없음
            eligible = new ArrayList<>(userMgmt.findEligible(me, cfg));
        }
        Collections.shuffle(eligible);

        int newbieTarget  = (int) Math.round(cfg.getPoolSize() * cfg.getNewbieRatio());
        int veteranTarget = cfg.getPoolSize() - newbieTarget;

        List<UserContext> newbies  = fillBucket(me, eligible, true,  newbieTarget,  cfg);
        List<UserContext> veterans = fillBucket(me, eligible, false, veteranTarget, cfg);

        List<UserContext> pool = new ArrayList<>(newbies);
        pool.addAll(veterans);
        backfill(pool, eligible, cfg);
        return pool;
    }

    /** 반경 20→50→100→전국(-1) 확장하며 target 채울 때까지. */
    private List<UserContext> fillBucket(UserContext me, List<UserContext> eligible,
                                         boolean wantNewbie, int target, MatchingConfig cfg) {
        LinkedHashSet<UserContext> bucket = new LinkedHashSet<>();
        List<UserContext> base = eligible.stream()
                .filter(u -> isNewbie(u, cfg) == wantNewbie)
                .toList();
        for (int radius : cfg.getRadiusStepsKm()) {
            double limit = radius < 0 ? Double.MAX_VALUE : radius;
            for (UserContext u : base) {
                if (bucket.contains(u)) continue;
                if (GeoUtil.haversine(me.lat(), me.lon(), u.lat(), u.lon()) <= limit) {
                    bucket.add(u);
                }
                if (bucket.size() >= target) return new ArrayList<>(bucket);
            }
        }
        return new ArrayList<>(bucket);
    }

    /** 양동이 합이 poolSize 미만이면 남은 eligible 로 채움 (부족 시 비율 양보). */
    private void backfill(List<UserContext> pool, List<UserContext> eligible, MatchingConfig cfg) {
        if (pool.size() >= cfg.getPoolSize()) return;
        Set<Long> picked = new HashSet<>();
        for (UserContext u : pool) picked.add(u.id());
        for (UserContext u : eligible) {
            if (pool.size() >= cfg.getPoolSize()) break;
            if (picked.add(u.id())) pool.add(u);
        }
    }

    private boolean isNewbie(UserContext u, MatchingConfig cfg) {
        return ChronoUnit.DAYS.between(u.signupAt(), LocalDate.now()) < cfg.getNewbieWindowDays();
    }

    /** 국내/해외 그룹 — UserManagement.findEligible 의 SQL 단계에서 이 메서드를 사용해 필터링. */
    public static boolean isDomestic(UserContext u) {
        return !"해외".equals(u.regionCountry());
    }
}
